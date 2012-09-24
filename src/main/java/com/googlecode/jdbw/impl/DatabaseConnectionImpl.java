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
package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.metadata.Catalog;
import com.googlecode.jdbw.metadata.MetaDataResolver;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * This is a default implementation of the {@code DatabaseConnection} interface,
 * you can construct objects of this type with a {@code Connection} or a 
 * {@code DataSource}.
 * @author Martin Berglund
 */
public class DatabaseConnectionImpl implements DatabaseConnection {

    private final DatabaseServerType databaseServerType;
    private final DataSource dataSource;
    private final DataSourceCloser dataSourceCloser;

    /**
     * Creates a {@code DatabaseConnectionImpl} backed by one physical database
     * connection. Since there is only one connection, you can only have one
     * ongoing operation against the database at one time. Trying to operate on
     * the database with multiple threads will serialize to one thread accessing
     * the database at a time through blocking. An active transaction counts as
     * an ongoing operation; failing to properly close it (by committing or 
     * rolling back) may leave the {@code DatabaseConnectionImpl} unusable with
     * some method calls blocking indefinitely.
     * 
     * <p>The type of the database server will be guessed, but you can also be
     * helpful and call the overloaded constructor which takes a 
     * {@code Connection} and a {@code DatabaseServerType} to specify what you
     * are connecting to.
     * @param connection Connection that is backing this {@code DatabaseConnection}
     * @throws IllegalArgumentException If the database server type could not be
     * figured out
     */
    public DatabaseConnectionImpl(Connection connection) {
        this(connection, null);
    }

    /**
     * Creates a {@code DatabaseConnectionImpl} backed by one physical database
     * connection. Since there is only one connection, you can only have one
     * ongoing operation against the database at one time. Trying to operate on
     * the database with multiple threads will serialize to one thread accessing
     * the database at a time through blocking. An active transaction counts as
     * an ongoing operation; failing to properly close it (by committing or 
     * rolling back) may leave the {@code DatabaseConnectionImpl} unusable with
     * some method calls blocking indefinitely.
     * 
     * @param connection Connection that is backing this {@code DatabaseConnection}
     * @param databaseServerType Type of the database server
     */
    public DatabaseConnectionImpl(Connection connection, DatabaseServerType databaseServerType) {
        this(new OneSharedConnectionDataSource(connection),
                new DataSourceCloser() {

                    public void closeDataSource(DataSource dataSource) {
                        ((OneSharedConnectionDataSource) dataSource).close();
                    }
                },
                databaseServerType);
    }
    
    /**
     * Creates a {@code DatabaseConnectionImpl} backed by a {@code DataSource}.
     * The behavior of the constructed {@code DatabaseConnectionImpl} is very
     * much determined by what kind of {@code DataSource} you are passing in as
     * the first parameter. Typically, this will be a connection pool and as 
     * such you will be able to operated on this {@code DatabaseConnectionImpl} 
     * with multiple threads in parallel. Normally, if the pool is starved and
     * cannot allocate any more connection, it will block until a connection
     * is available. In this case, the {@code DatabaseConnectionImpl} will block
     * too, when calling the methods that utilize the database connection. If 
     * the {@code DataSource} throws an exception when no more connections can
     * be allocated, so will the {@code DatabaseConnectionImpl}.
     * 
     * <p>Please note that a {@code DataSource} doesn't expose any method for 
     * closing it, so calling {@code close()} on this 
     * {@code DatabaseConnectionImpl} won't do anything. If you want to be able
     * to close the underlying source through this object, please use one of the
     * constructor overloads with a {@code DataSourceCloser} parameter.
     * 
     * <p>The type of the database server will be guessed, but you can also be
     * helpful and call an overloaded constructor which takes a 
     * {@code DatabaseServerType} to specify what you are connecting to.
     * 
     * @param dataSource Underlying database connection supplier
     * @throws IllegalArgumentException If the database server type could not be
     * figured out
     */
    public DatabaseConnectionImpl(DataSource dataSource) {
        this(dataSource, null, null);
    }

    /**
     * Creates a {@code DatabaseConnectionImpl} backed by a {@code DataSource}.
     * The behavior of the constructed {@code DatabaseConnectionImpl} is very
     * much determined by what kind of {@code DataSource} you are passing in as
     * the first parameter. Typically, this will be a connection pool and as 
     * such you will be able to operated on this {@code DatabaseConnectionImpl} 
     * with multiple threads in parallel. Normally, if the pool is starved and
     * cannot allocate any more connection, it will block until a connection
     * is available. In this case, the {@code DatabaseConnectionImpl} will block
     * too, when calling the methods that utilize the database connection. If 
     * the {@code DataSource} throws an exception when no more connections can
     * be allocated, so will the {@code DatabaseConnectionImpl}.
     * 
     * <p>Please note that a {@code DataSource} doesn't expose any method for 
     * closing it, so calling {@code close()} on this 
     * {@code DatabaseConnectionImpl} won't do anything. If you want to be able
     * to close the underlying source through this object, please use one of the
     * constructor overloads with a {@code DataSourceCloser} parameter.
     * 
     * @param dataSource Underlying database connection supplier
     * @param databaseServerType Type of the database server
     */
    public DatabaseConnectionImpl(DataSource dataSource, DatabaseServerType databaseServerType) {
        this(dataSource, null, databaseServerType);
    }

    /**
     * Creates a {@code DatabaseConnectionImpl} backed by a {@code DataSource}.
     * The behavior of the constructed {@code DatabaseConnectionImpl} is very
     * much determined by what kind of {@code DataSource} you are passing in as
     * the first parameter. Typically, this will be a connection pool and as 
     * such you will be able to operated on this {@code DatabaseConnectionImpl} 
     * with multiple threads in parallel. Normally, if the pool is starved and
     * cannot allocate any more connection, it will block until a connection
     * is available. In this case, the {@code DatabaseConnectionImpl} will block
     * too, when calling the methods that utilize the database connection. If 
     * the {@code DataSource} throws an exception when no more connections can
     * be allocated, so will the {@code DatabaseConnectionImpl}.
     * 
     * <p>Please note that a {@code DataSource} doesn't expose any method for 
     * closing it, that why this constructor takes a {@code DataSourceCloser}
     * interface as a parameter. It is assumed the closer knows how to close 
     * this particular {@code DataSource}. If you don't need to close the 
     * {@code DataSource} (through this {@code DatabaseConnectionImpl}), you can
     * pass null for this parameter.
     * 
     * <p>The type of the database server will be guessed, but you can also be
     * helpful and call the overloaded constructor which takes a 
     * {@code DataSource}, a {@code DataSourceCloser} and a 
     * {@code DatabaseServerType} to specify what you are connecting to.
     * 
     * @param dataSource Underlying database connection supplier
     * @param dataSourceCloser Object which knows how to close the data source,
     * or null if you want calls to {@code close()} be ignored
     * @throws IllegalArgumentException If the database server type could not be
     * figured out
     */
    public DatabaseConnectionImpl(DataSource dataSource, DataSourceCloser dataSourceCloser) {
        this(dataSource, dataSourceCloser, null);
    }

    

    /**
     * Creates a {@code DatabaseConnectionImpl} backed by a {@code DataSource}.
     * The behavior of the constructed {@code DatabaseConnectionImpl} is very
     * much determined by what kind of {@code DataSource} you are passing in as
     * the first parameter. Typically, this will be a connection pool and as 
     * such you will be able to operated on this {@code DatabaseConnectionImpl} 
     * with multiple threads in parallel. Normally, if the pool is starved and
     * cannot allocate any more connection, it will block until a connection
     * is available. In this case, the {@code DatabaseConnectionImpl} will block
     * too, when calling the methods that utilize the database connection. If 
     * the {@code DataSource} throws an exception when no more connections can
     * be allocated, so will the {@code DatabaseConnectionImpl}.
     * 
     * <p>Please note that a {@code DataSource} doesn't expose any method for 
     * closing it, that why this constructor takes a {@code DataSourceCloser}
     * interface as a parameter. It is assumed the closer knows how to close 
     * this particular {@code DataSource}. If you don't need to close the 
     * {@code DataSource} (through this {@code DatabaseConnectionImpl}), you can
     * pass null for this parameter.
     * 
     * @param dataSource Underlying database connection supplier
     * @param dataSourceCloser Object which knows how to close the data source,
     * or null if you want calls to {@code close()} be ignored
     * @param databaseServerType Type of the database server
     */
    public DatabaseConnectionImpl(DataSource dataSource, DataSourceCloser dataSourceCloser, DatabaseServerType databaseServerType) {
        this.dataSource = dataSource;
        this.dataSourceCloser = dataSourceCloser;
        if(databaseServerType != null)
            this.databaseServerType = databaseServerType;
        else
            this.databaseServerType = guessDatabaseServerType(dataSource);
    }

    @Override
    public TransactionIsolation getDefaultTransactionIsolation() {
        Connection connection = null;
        try {
            try {
                connection = getConnection();
                return TransactionIsolation.fromLevel(connection.getTransactionIsolation());
            }
            catch(SQLException e) {}
        }
        finally {
            if(connection != null) {
                try {
                    connection.close();
                }
                catch(SQLException e) {}
            }
        }
        return null;
    }

    @Override
    public void close() {
        if(dataSourceCloser != null)
            dataSourceCloser.closeDataSource(dataSource);
    }

    @Override
    public DatabaseTransaction beginTransaction(TransactionIsolation transactionIsolation) throws SQLException {
        Connection connection = getConnection();
        return new DatabaseTransactionImpl(
                connection, 
                getServerType().createExecutor(connection),
                transactionIsolation);
    }

    @Override
    public AutoExecutor createAutoExecutor() {
        return getServerType().createAutoExecutor(dataSource);
    }
    
    Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Catalog> getCatalogs() throws SQLException {
        MetaDataResolver metaDataResolver = createMetaDataResolver();
        return metaDataResolver.getCatalogs();
    }

    @Override
    public Catalog getCatalog(String catalogName) throws SQLException {
        MetaDataResolver metaDataResolver = createMetaDataResolver();
        return metaDataResolver.getCatalog(catalogName);
    }

    @Override
    public DatabaseServerType getServerType() {
        return databaseServerType;
    }

    public String getDefaultCatalogName() {
        Connection connection = null;
        try {
            try {
                connection = getConnection();
                return connection.getCatalog();
            }
            catch(SQLException e) {}
        }
        finally {
            if(connection != null) {
                try {
                    connection.close();
                }
                catch(SQLException e) {}
            }
        }
        return null;
    }

    protected MetaDataResolver createMetaDataResolver() {
        return getServerType().createMetaDataResolver(dataSource);
    }

    /**
     * Try to guess what database connection was passed in
     */
    private DatabaseServerType guessDatabaseServerType(DataSource dataSource) {
        throw new IllegalArgumentException("Could not guess the database type of "
                + "the supplied data source");
    }
}
