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
package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.DatabaseServerType;

/**
 * Describes a DatabaseConnectionFactory that can specify a username and a password to use when connecting to the
 * database server.
 * @author Martin Berglund
 */
public class AuthenticatingDatabaseConnectionFactory extends BasicDatabaseConnectionFactory {

    /**
     * Creates a new connection factory with a specified type and JDBC url
     * @param databaseServerType What type of database server this connection factory creates connections for
     * @param jdbcUrl The URL to use when creating connections to this database server
     */
    public AuthenticatingDatabaseConnectionFactory(
            DatabaseServerType databaseServerType, 
            String jdbcUrl) {
        
        super(databaseServerType, jdbcUrl);
    }

    /**
     * Sets the username property to a specific value, this is generally what's used when logging in to remote database
     * servers that requires authentication
     * @param username What username to use
     * @return Itself
     */
    public AuthenticatingDatabaseConnectionFactory setUsername(String username) {
        setConnectionProperty("user", username);
        return this;
    }

    /**
     * Sets the password property to a specific value, this is generally what's used when logging in to remote database
     * servers that requires authentication
     * @param password What password to use
     * @return Itself
     */
    public AuthenticatingDatabaseConnectionFactory setPassword(String password) {
        setConnectionProperty("password", password);
        return this;
    }
}
