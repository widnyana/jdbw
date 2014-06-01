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

import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.JDBCDriverDescriptor;

/**
 *
 * @author Martin Berglund
 */
public class H2JDBCDriverDescriptor implements JDBCDriverDescriptor {

    public String formatJDBCUrlForAnonymousInMemory() {
        return "jdbc:h2:mem:";
    }
    
    public String formatJDBCUrlForInMemory(String name) {
        return "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1";
    }
    
    public String formatJDBCUrlForFile(String databaseFilePrefix) {
        return "jdbc:h2:file:" + databaseFilePrefix;
    }
    
    @Override
    public String formatJDBCUrl(DatabaseServer databaseServer) {
        if(databaseServer instanceof H2NetworkServer == false) {
            throw new IllegalArgumentException("H2JDBCDriverDescriptor only supports H2NetworkServer");
        }
        H2NetworkServer h2Server = (H2NetworkServer)databaseServer;
        String host = h2Server.getHostname();
        int port = h2Server.getPort();
        String defaultCatalog = h2Server.getCatalog();
        return "jdbc:h2:tcp://" + host + ":" + port + "/" + defaultCatalog; //What to do about the path to the database???
    }

    @Override
    public String getDriverClassName() {
        return "org.h2.Driver";
    }
}
