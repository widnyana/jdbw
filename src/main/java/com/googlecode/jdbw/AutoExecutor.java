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
 * Copyright (C) 2007-2012 Martin Berglund
 */
package com.googlecode.jdbw;

import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

/**
 * An auto executor will automatically allocate a connection from the pool when its SQL methods are called and return 
 * the connection and all resources once done. If a query throws an exception, the exception will be evaluated and if
 * it's classified as a connection error there is logic to automatically allocate a new connection and retry. You can 
 * customize how long to wait between retries, how many time to retry and what transaction isolation to use when 
 * executing the query. All queries are executed in auto-commit mode.
 *
 * @author Martin Berglund
 */
public class AutoExecutor implements SQLExecutor {

    private final DataSource dataSource;
    private final DatabaseServerType serverType;
    private final TransactionIsolation transactionIsolation;
    private final int connectionErrorRetryInterval;
    private final TimeUnit connectionErrorRetryIntervalTimeUnit;
    private final int connectionErrorNrOfRetries;

    /**
     * Creates a new AutoExecutor using READ_UNCOMMITTED isolation with an unlimited connection error retry limit and a 
     * retry interval at 500 milliseconds.
     * @param dataSource DataSource to draw connections from
     * @param serverType Server type of the connections
     */
    public AutoExecutor(DataSource dataSource, DatabaseServerType serverType) {
        this(dataSource, serverType, TransactionIsolation.READ_UNCOMMITTED);
    }

    /**
     * Creates a new AutoExecutor using a specified transaction isolation with an unlimited connection error retry limit 
     * and a retry interval at 500 milliseconds.
     * @param dataSource DataSource to draw connections from
     * @param serverType Server type of the connections
     * @param transactionIsolation Transaction isolation to use
     */
    public AutoExecutor(DataSource dataSource, DatabaseServerType serverType, TransactionIsolation transactionIsolation) {
        this(dataSource, serverType, transactionIsolation, 500, TimeUnit.MILLISECONDS, -1);
    }

    /**
     * Creates a new AutoExecutor with READ_UNCOMMITTED isolation, a specified number of connection retries on 
     * connection error and a retry interval at 500 milliseconds.
     * @param dataSource DataSource to draw connections from
     * @param serverType Server type of the connections
     * @param connectionErrorNrOfRetries How many times to retry a query on connection error (-1 means unlimited)
     */
    public AutoExecutor(DataSource dataSource, DatabaseServerType serverType, int connectionErrorNrOfRetries) {
        this(dataSource, serverType, TransactionIsolation.READ_UNCOMMITTED, 500, TimeUnit.MILLISECONDS, connectionErrorNrOfRetries);
    }    
    

    /**
     * Creates a new AutoExecutor with a specified isolation, a specified number of retries on connection error and a 
     * specified retry interval.
     * @param dataSource DataSource to draw connections from
     * @param serverType Server type of the connections
     * @param transactionIsolation Transaction isolation level to use when running each statement
     * @param connectionErrorRetryInterval How long to sleep between the connection retries
     * @param connectionErrorRetryIntervalTimeUnit Unit of the {@link connectionErrorRetryInterval}
     * @param connectionErrorNrOfRetries How many times to retry a query on connection error (-1 means unlimited)
     */
    public AutoExecutor(DataSource dataSource, 
            DatabaseServerType serverType, 
            TransactionIsolation transactionIsolation,
            int connectionErrorRetryInterval, 
            TimeUnit connectionErrorRetryIntervalTimeUnit, 
            int connectionErrorNrOfRetries) {
        
        this.dataSource = dataSource;
        this.serverType = serverType;
        this.transactionIsolation = transactionIsolation;
        this.connectionErrorRetryInterval = connectionErrorRetryInterval;
        this.connectionErrorRetryIntervalTimeUnit = connectionErrorRetryIntervalTimeUnit;
        this.connectionErrorNrOfRetries = connectionErrorNrOfRetries;
        
        assert this.connectionErrorNrOfRetries >= -1;
        assert this.connectionErrorRetryIntervalTimeUnit != null;
    }

    /**
     * Shortcut for calling execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
     * @param SQL SQL to execute on the remote database server, with ? marking each parameter
     * @param parameters List of parameters to substitute each ? in the SQL
     * @throws java.sql.SQLException
     */
    public void execute(String SQL, Object... parameters) throws SQLException {
        execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
    }
    
    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException {
        execute(handler, 0, 0, SQL, parameters);
    }

    @Override
    public void execute(ExecuteResultHandler handler, int maxRowsToFetch, int queryTimeoutInSeconds, String SQL, Object... parameters) throws SQLException {
        Connection connection = null;
        int attempt = 0;
        while(connectionErrorNrOfRetries == -1 || connectionErrorNrOfRetries > attempt) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.execute(handler, maxRowsToFetch, queryTimeoutInSeconds, SQL, parameters);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(connectionErrorRetryIntervalTimeUnit.toMillis(connectionErrorRetryInterval));
                    attempt++;
                }
                else {
                    throw e;  //Syntax error?
                }
            }
            finally {
                if(connection != null) {
                    connection.close();
                }
            }
        }
    }

    /**
     * Shortcut for calling batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL, parameters);
     * @param SQL SQL to run on the remote database server, with ? marking a parameter
     * @param parameters List of parameters for the SQL query. This controls how large the batch will be, each element
     * in the list corresponds to one write in the batch.
     * @throws java.sql.SQLException
     */
    public void batchWrite(String SQL, List<Object[]> parameters) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), SQL, parameters);
    }
    
    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException {
        Connection connection = null;
        int attempt = 0;
        while(connectionErrorNrOfRetries == -1 || connectionErrorNrOfRetries > attempt) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.batchWrite(handler, SQL, parameters);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(connectionErrorRetryIntervalTimeUnit.toMillis(connectionErrorRetryInterval));
                    attempt++;
                }
                else {
                    throw e;  //Syntax error?
                }
            }
            finally {
                if(connection != null) {
                    connection.close();
                }
            }
        }
    }

    /**
     * Shortcut for calling batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
     * @param batchedSQL List SQL to execute on the remote database server in one batch.
     * @throws java.sql.SQLException
     */
    public void batchWrite(List<String> batchedSQL) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
    }
    
    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException {
        Connection connection = null;
        int attempt = 0;
        while(connectionErrorNrOfRetries == -1 || connectionErrorNrOfRetries > attempt) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.batchWrite(handler, batchedSQL);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(connectionErrorRetryIntervalTimeUnit.toMillis(connectionErrorRetryInterval));
                    attempt++;
                }
                else {
                    throw e;
                }
            }
            finally {
                if(connection != null) {
                    connection.close();
                }
            }
        }
    }

    private SQLExecutor createSQLExecutor(Connection connection) {
        return serverType.createExecutor(connection);
    }

    private Connection getNewConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        connection.setTransactionIsolation(transactionIsolation.getConstant());
        return connection;
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {
        }
    }
}
