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

package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.server.MultiCatalogDatabaseServer;
import com.googlecode.jdbw.server.NetworkDatabaseServer;
import com.googlecode.jdbw.server.StandardDatabaseServer;
import com.googlecode.jdbw.server.UserAuthenticatedDatabaseServer;
import java.util.Properties;

/**
 *
 * @author mabe02
 */
public class H2NetworkServer extends StandardDatabaseServer implements NetworkDatabaseServer, MultiCatalogDatabaseServer, UserAuthenticatedDatabaseServer, H2Server {

    public H2NetworkServer(String hostname, int port, String catalog, String username, String password) {
        super(new H2JDBCDriverDescriptor(), hostname, port, catalog, username, password);
    }

    public DatabaseServerType getServerType() {
        return new H2ServerTypes.Network();
    }
    
    @Override
    public Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("USER", getUsername());
        properties.setProperty("PASSWORD", getPassword());
        return properties;
    }
}
