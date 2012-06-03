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

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.metadata.Catalog;
import com.googlecode.jdbw.metadata.MetaDataResolver;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author mabe02
 */
public class DatabaseConnectionImpl implements DatabaseConnection {

    private final DatabaseServer databaseServer;
    private final DataSource dataSource;
    private final DataSourceCloser dataSourceCloser;

    public DatabaseConnectionImpl(Connection connection) {
        this(connection, null);
    }

    public DatabaseConnectionImpl(Connection connection, DatabaseServer databaseServer) {
        this(new OneSharedConnectionDataSource(connection),
                new DataSourceCloser() {

                    public void closeDataSource(DataSource dataSource) {
                        ((OneSharedConnectionDataSource) dataSource).close();
                    }
                },
                databaseServer);
    }

    public DatabaseConnectionImpl(DataSource dataSource, DataSourceCloser dataSourceCloser) {
        this(dataSource, dataSourceCloser, null);
    }

    public DatabaseConnectionImpl(DataSource dataSource, DataSourceCloser dataSourceCloser, DatabaseServer databaseServer) {
        this.dataSource = dataSource;
        this.databaseServer = databaseServer;
        this.dataSourceCloser = dataSourceCloser;
    }

    @Override
    public TransactionIsolation getDefaultTransactionIsolation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
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
        return databaseServer.getServerType();
    }

    public String getDefaultCatalogName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected MetaDataResolver createMetaDataResolver() {
        return new MetaDataResolver(dataSource);
    }
}
