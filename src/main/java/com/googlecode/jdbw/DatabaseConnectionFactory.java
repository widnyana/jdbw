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

/**
 * Factory interface for creating DatabaseConnection:s. Using this factory, you can customize the connection parameters
 * used when creating a DataSource, though usually you would not use this interface directly but through one of several
 * sub-interfaces or implementing classes tuned for a particular JDBC driver and/or database server.
 * @author Martin Berglund
 */
public interface DatabaseConnectionFactory {
    /**
     * Provides an extra property key-value to be passed to the JDBC driver when connecting, please see your JDBC 
     * drivers documentation for more information on what properties are available.
     * @param propertyName Name of the property
     * @param value Value of the property
     * @return Returns itself, so you can easily chain multiple calls together
     */
    DatabaseConnectionFactory setConnectionProperty(String propertyName, String value);
    
    /**
     * Establishes a database connection using the currently configured values of this DatabaseConnectionFactory and the
     * DataSourceFactory passed in, returning a DatabaseConnection object representing this connection.
     * @param dataSourceFactory DataSourceFactory to use when creating the underlying DataSource for the returned
     * DatabaseConnection
     * @return DatabaseConnection containing a DataSource configured for the database server this connection factory has
     * been specified for
     */
    DatabaseConnection connect(DataSourceFactory dataSourceFactory);
}
