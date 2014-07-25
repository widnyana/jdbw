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
package com.googlecode.jdbw.server.postgresql;

import com.googlecode.jdbw.DatabaseConnectionFactory;
import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;
import com.googlecode.jdbw.server.StandardDatabaseServer;

/**
 * This class represents a PostgreSQL server connected to over a TCP/IP network.
 * 
 * @see StandardDatabaseServer
 * @author Martin Berglund
 */
public class PostgresqlServer extends StandardDatabaseServer {
    public PostgresqlServer(
            String hostname, 
            int port, 
            String catalog) {
        this(new PostgresqlJDBCDriverDescriptor(), hostname, port, catalog);
    }

    protected PostgresqlServer(
            JDBCDriverDescriptor driverDescriptor, 
            String hostname, 
            int port, 
            String catalog) {
        super(driverDescriptor, hostname, port, catalog);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerTypes.POSTGRESQL;
    }
}
