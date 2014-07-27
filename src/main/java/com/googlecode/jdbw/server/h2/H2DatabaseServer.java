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
package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.DataSourceFactory;
import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.server.AbstractDatabaseServer;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;

/**
 *
 * @author Martin Berglund
 */
public abstract class H2DatabaseServer extends AbstractDatabaseServer {
    private final H2ServerType serverType;
    private final boolean allowMultipleConnections;

    protected H2DatabaseServer(H2ServerType serverType, boolean allowMultipleConnections) {
        super(new H2JDBCDriverDescriptor());
        this.serverType = serverType;
        this.allowMultipleConnections = allowMultipleConnections;
    }

    /**
     * Creates a new DatabaseConnection to this H2 server that is backed by a single connection
     * @return DatabaseConnection established to this H2 server
     */
    public DatabaseConnection connect() {
        return connect(new OneSharedConnectionDataSource.Factory());
    }

    /**
     * Creates a new DatabaseConnection to this H2 server that is backed by a DataSource created by the
     * DataSourceFactory parameter supplied. If you need to tweak the connection parameters further, please use the
     * newConnectionFactory(..) method instead.
     * @param dataSourceFactory Factory implementation that will create the DataSource that backs the DatabaseConnection
     *                          that is to be created
     * @return DatabaseConnection established to this H2 server
     */
    public DatabaseConnection connect(DataSourceFactory dataSourceFactory) {
        if(!isAllowMultipleConnections() && !(dataSourceFactory instanceof OneSharedConnectionDataSource.Factory)) {
            throw new IllegalArgumentException("You can't connect to a " + this + " with multiple connections");
        }
        return newConnectionFactory().connect(dataSourceFactory);
    }
    
    @Override
    public DatabaseServerType getServerType() {
        return serverType;
    }

    boolean isAllowMultipleConnections() {
        return allowMultipleConnections;
    }

    abstract String getJDBCUrl(H2JDBCDriverDescriptor driverDescriptor);
}
