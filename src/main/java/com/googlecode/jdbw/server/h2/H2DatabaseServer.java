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

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.server.AbstractDatabaseServer;

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
    
    @Override
    public DatabaseServerType getServerType() {
        return serverType;
    }

    @Override
    public H2DatabaseConnectionFactory newConnectionFactory() {
        return new H2DatabaseConnectionFactory(serverType, getDriverDescriptor().formatJDBCUrl(this), allowMultipleConnections);
    }

    abstract String getJDBCUrl(H2JDBCDriverDescriptor driverDescriptor);
}
