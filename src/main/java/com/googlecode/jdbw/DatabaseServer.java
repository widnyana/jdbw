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

package com.googlecode.jdbw;

import java.sql.SQLException;

/**
 * This interface represents a database server and exposes methods for 
 * inspecting the servers characteristics and establishing a 
 * {@code DatabaseConnection} to it.
 * @author mabe02
 */
public interface DatabaseServer {

    /**
     * @return Type of the server
     */
    DatabaseServerType getServerType();
    
    /**
     * Sets a property to be passed in when creating a database connection. These user-set 
     * properties are added to the connection properties list at the end, overriding any properties
     * that may already have been created by the DatabaseServer implementation.
     * @param key Name of the JDBC connection property
     * @param value Value for the JDBC connection property
     */
    void setConnectionProperty(String key, String value);
    
    /**
     * Created a new DataSource to this server and returns it wrapped in a 
     * DatabaseConnection
     * @param dataSourceFactory Factory to use when creating the DataSource
     * @return DatabaseConnection connected to the server
     */
    DatabaseConnection connect(DataSourceFactory dataSourceFactory);
    
    /**
     * Tries to create a new database connection and immediately close it. If there is any 
     * connection errors when trying to create the connection, the method will throw the 
     * corresponding SQLException that was thrown by the JDBC driver.
     * 
     * @throws SQLException In case there was an error connecting to the database server
     */
    void testConnection() throws SQLException;
}
