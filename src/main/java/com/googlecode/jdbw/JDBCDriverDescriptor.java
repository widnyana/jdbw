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
 * This interface describes a JDBC driver and how to use it. You'll need to
 * implement this interface in order to use a custom JDBC driver not bundled
 * with jdbw.
 * @see com.googlecode.jdbw.server.mysql.MySQLJDBCDriverDescriptor
 * @author Martin Berglund
 */
public interface JDBCDriverDescriptor<T extends DatabaseConnectionFactory> {
    /**
     * @return The full path of the JDBC driver class to be loaded
     */
    String getDriverClassName();
    
    /**
     * Given a database server (assumed to be compatible with this driver descriptor, or it will throw 
     * IllegalArgumentException), format a JDBC URL that can be used to connect to the server.
     * @param databaseServer Database server to connect to
     * @return JDBC url formatted for this driver
     * @throws IllegalArgumentException If the databaseServer is not supported by this driver descriptor
     */
    String formatJDBCUrl(DatabaseServer databaseServer);

    T createDatabaseConnectionFactory(DatabaseServer databaseServer);
}
