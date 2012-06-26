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
 * Copyright (C) 2007-2012 mabe02
 */
package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.server.StandardDatabaseServer;
import java.util.Properties;

/**
 * This class represents a MySQL database server that is connected to over a
 * TCP/IP network. 
 * @author mabe02
 */
public class MySQLServer extends StandardDatabaseServer {

    public MySQLServer(String hostname, int port, String catalog, String username, String password) {
        this(new MySQLDefaultJDBCDriverDescriptor(), hostname, port, catalog, username, password);
    }

    public MySQLServer(JDBCDriverDescriptor driverDescriptor, String hostname, int port, String catalog, String username, String password) {
        super(driverDescriptor, hostname, port, catalog, username, password);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerTypes.MYSQL;
    }

    @Override
    public Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", getUsername());
        properties.setProperty("password", getPassword());
        return properties;
    }
}
