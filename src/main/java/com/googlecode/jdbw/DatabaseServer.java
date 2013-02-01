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

package com.googlecode.jdbw;

import java.sql.SQLException;

/**
 * This interface represents a database server and exposes methods for 
 * inspecting the servers characteristics and establishing a 
 * {@code DatabaseConnection} to it.
 * @author Martin Berglund
 */
public interface DatabaseServer {

    /**
     * @return Type of the server
     */
    DatabaseServerType getServerType();
    
    /**
     * Creates a new {@link DatabaseConnectionFactory} that can be used for establishing a JDBC connection to this database.
     * @param username Username to use when connecting to the server
     * @param password Password to use when connecting to the server
     * @return A {@link DatabaseConnectionFactory} targeting this database server
     */
    DatabaseConnectionFactory newConnectionFactory();
    
    /**
     * Tries to create a new database connection and immediately close it. If there is any 
     * connection errors when trying to create the connection, the method will throw the 
     * corresponding SQLException that was thrown by the JDBC driver.
     * 
     * @param username Username to use when testing the connection
     * @param password Password to use when testing the connection
     * @throws SQLException In case there was an error connecting to the database server
     */
    void testConnection(String username, String password) throws SQLException;
}
