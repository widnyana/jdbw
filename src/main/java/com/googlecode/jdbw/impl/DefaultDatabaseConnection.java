/*
 * This file is part of jdbw (http://code.google.com/p/jdbw/).
 * 
 * jdbw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009-2012 mabe02
 */

package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.server.StandardDatabaseServer;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author mabe02
 */
public class DefaultDatabaseConnection implements DatabaseConnection
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseConnection.class);
    private static final int MAX_EXCEPTION_QUEUE_SIZE = 1000;

    private final StandardDatabaseServer databaseServer;
    private final ManagerThread managerThread;
    private final ConnectionPool connectionPool;
    
    private int maxNumberOfConnections;
    private boolean closed;
    
    private Queue<SQLException> exceptionQueue;
    private List<DefaultDatabaseConnectionListener> listeners;

    protected DefaultDatabaseConnection(StandardDatabaseServer databaseServer)
    {                
        this.closed = false;
        this.maxNumberOfConnections = 1;
        this.databaseServer = databaseServer;
        
        this.managerThread = new ManagerThread();
        this.connectionPool = new ConnectionPool();
        this.exceptionQueue = new ConcurrentLinkedQueue<SQLException>();
        this.listeners = new ArrayList<DefaultDatabaseConnectionListener>();
    }

    void start()
    {
        managerThread.start();
    }

    @Override
    public TransactionIsolation getDefaultTransactionIsolation()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SQLException getNextConnectionError()
    {
        return exceptionQueue.poll();
    }

    @Override
    public void close()
    {
        closed = true;
        try {
            managerThread.join();
        }
        catch(InterruptedException e) {}
    }

    @Override
    public DatabaseTransaction beginTransaction(TransactionIsolation transactionIsolation)
    {
        PooledDatabaseConnection pooledConnection = getPooledConnection();
        final DefaultDatabaseTransaction databaseConnection = new DefaultDatabaseTransaction(pooledConnection, transactionIsolation);
        pooledConnection.connectionUser(databaseConnection);
        return databaseConnection;
    }

    @Override
    public SQLExecutor createAutoExecutor()
    {
        return new DefaultAutoExecutor(this);
    }

    public DataSource createDataSource() 
    {
        return new DataSource() {
            public Connection getConnection() throws SQLException {
                PooledDatabaseConnection pooledConnection = getPooledConnection();
                DataSourcedConnection dataSourcedConnection = new DataSourcedConnection(pooledConnection);
                pooledConnection.connectionUser(dataSourcedConnection);
                return dataSourcedConnection;
            }

            public Connection getConnection(String username, String password) throws SQLException {
                return getConnection();
            }

            public PrintWriter getLogWriter() throws SQLException {
                throw new UnsupportedOperationException("getLogWriter() is not supported.");
            }

            public void setLogWriter(PrintWriter out) throws SQLException {
                throw new UnsupportedOperationException("setLogWriter() is not supported.");
            }

            public void setLoginTimeout(int seconds) throws SQLException {
                throw new UnsupportedOperationException("setLoginTimeout() is not supported.");
            }

            public int getLoginTimeout() throws SQLException {
                throw new UnsupportedOperationException("getLoginTimeout() is not supported.");
            }

            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new SQLException("DefaultDatabaseConnection$DataSource is not a wrapper.");
            }

            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }
        };
    }

    @Override
    public String getDefaultCatalogName()
    {
        return databaseServer.getCatalog();
    }

    @Override
    public List<Catalog> getCatalogs() throws SQLException
    {
        MetaDataResolver metaDataResolver = createMetaDataResolver();
        return metaDataResolver.getCatalogs();
    }

    @Override
    public Catalog getCatalog(String catalogName) throws SQLException
    {
        MetaDataResolver metaDataResolver = createMetaDataResolver();
        return metaDataResolver.getCatalog(catalogName);
    }

    @Override
    public DatabaseServerTraits getTraits()
    {
        return databaseServer.getServerTraits();
    }

    @Override
    public DatabaseServerType getServerType()
    {
        return databaseServer.getServerType();
    }

    @Override
    public void addListener(DefaultDatabaseConnectionListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(DefaultDatabaseConnectionListener listener)
    {
        listeners.remove(listener);
    }

    public void reconnect() {
        while(connectionPool.removeFreeConnection() != null);
    }

    public int getPoolSize() {
        return maxNumberOfConnections;
    }

    public void setPoolSize(int poolSize) {
        if(poolSize > 0)
            this.maxNumberOfConnections = poolSize;
    }
    
    protected MetaDataResolver createMetaDataResolver()
    {
        return new DefaultMetaDataResolver(this);
    }

    void createConnection() throws SQLException {
        allocateNewConnection();
    }

    PooledDatabaseConnection getPooledConnection()
    {
        return connectionPool.aquireConnection();
    }

    boolean isConnectionError(SQLException e)
    {
        return databaseServer.isConnectionError(e);
    }

    protected PooledDatabaseConnection newPooledDatabaseConnection(Connection connection)
    {
        return new PooledDatabaseConnection(connection);
    }

    private void allocateNewConnection() throws SQLException
    {
        try {
            Connection connection = DriverManager.getConnection(databaseServer.getJDBCUrl(), databaseServer.getConnectionProperties());
            connection.setAutoCommit(true);
            LOGGER.log(Level.FINE, "Allocated a new connection thread to {0}", databaseServer.toString());

            PooledDatabaseConnection pooledConnection = newPooledDatabaseConnection(connection);
            connectionPool.addNewConnection(pooledConnection);
        }
        catch(SQLException e) {
            exceptionQueue.add(e);
            while(exceptionQueue.size() > MAX_EXCEPTION_QUEUE_SIZE)
                exceptionQueue.poll();
            throw e;
        }
    }

    private class ManagerThread extends Thread
    {
        public ManagerThread()
        {
            super(databaseServer.toString() + " pool manager");
        }

        @Override
        public void run()
        {
            int subsequentConnectionFailedCount = 0;
            
            while(!closed)
            {
                //Sleep 5 ms before checking
                DefaultDatabaseConnection.this.sleep(5);

                //Step 1: allocate new connections if needed
                if(connectionPool.getTotalConnections() < maxNumberOfConnections) {
                    try {
                        allocateNewConnection();
                        subsequentConnectionFailedCount = 0;
                        for(DefaultDatabaseConnectionListener listener: new ArrayList<DefaultDatabaseConnectionListener>(listeners))
                            listener.onConnectionEstablished();
                    }
                    catch(SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error allocating a new connection to {0}: {1}",
                                new Object[]{databaseServer.toString(), e.getMessage()});

                        for(DefaultDatabaseConnectionListener listener: listeners)
                            listener.onConnectionFailed(e);

                        //If we have 10 failed connection attempts, sleep a bit longer
                        if(++subsequentConnectionFailedCount >= 10)
                            DefaultDatabaseConnection.this.sleep(30 * 1000);
                        else
                            DefaultDatabaseConnection.this.sleep(1 * 1000);
                    }
                }
                //Step 2: close excessive connections, if any
                else if(connectionPool.getTotalConnections() > maxNumberOfConnections) {
                    PooledDatabaseConnection connection = connectionPool.removeFreeConnection();
                    if(connection != null) {
                        connection.close();
                        LOGGER.log(Level.FINE, "Closed a connection to {0}", databaseServer.toString());
                    }
                }
                //Step 3: return done connections and close bad connections
                else if(connectionPool.returnDoneConnections() || 
                        connectionPool.dropBadConnections()) {
                    //Nothing?
                }
                //Step 4: Keep-alive on unused connections
                else {
                    connectionPool.pingUnusedConnections();
                }
            }

            //Exiting, so close all connections
            while(connectionPool.getTotalConnections() > 0) {
                connectionPool.returnDoneConnections();
                connectionPool.dropBadConnections();
                connectionPool.removeFreeConnection();
                LOGGER.log(Level.FINE, "Closed a connection to {0}", databaseServer.toString());
                Thread.yield();
            }
        }
    }

    private void sleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {}
    }
}
