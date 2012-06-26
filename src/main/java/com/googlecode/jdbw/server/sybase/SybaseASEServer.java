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

package com.googlecode.jdbw.server.sybase;

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.server.StandardDatabaseServer;
import java.util.Properties;

/**
 * This class represents a Sybase ASE server connected to over a TCP/IP network.
 * While all versions of Sybase ASE has not been tested for compatibility,
 * indeed this mostly depends on the driver, versions 12.x to 15.x are expected
 * to work with the functionality exposed by JDBW.
 * 
 * @see StandardDatabaseServer
 * @author mabe02
 */
public class SybaseASEServer extends StandardDatabaseServer {
    
    private final String processName;
    
    public SybaseASEServer(
            String hostname, 
            int port, 
            String catalog, 
            String username, 
            String password, 
            String processName) {
        this(new SybaseJConn3JDBCDriverDescriptor(), hostname, port, catalog, username, password, processName);
    }

    public SybaseASEServer(
            JDBCDriverDescriptor driverDescriptor, 
            String hostname, 
            int port, 
            String catalog, 
            String username, 
            String password,
            String processName) {
        super(driverDescriptor, hostname, port, catalog, username, password);
        this.processName = processName;
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerTypes.SYBASE_ASE;
    }

    @Override
    public Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("APPLICATIONNAME", processName);
        properties.setProperty("USER", getUsername());
        properties.setProperty("PASSWORD", getPassword());
        properties.setProperty("CHARSET", "utf8");
        return properties;
    }
}
