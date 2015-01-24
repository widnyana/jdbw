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
 * This interface describes a JDBC driver and how to use it. You'll need to implement this interface in order to use a
 * custom JDBC driver not bundled with jdbw. The reason why this interface exists is because a single database server
 * type might have several different JDBC implementing drivers, each being somewhat different in the way they
 * communicate with the server and what properties they use. In general though, there is only one JDBC driver per
 * database server.
 * @see com.googlecode.jdbw.server.mysql.MySQLJDBCDriverDescriptor
 * @author Martin Berglund
 */
public interface JDBCDriverDescriptor<T extends DatabaseConnectionFactory> {
    /**
     * This method should return the full class name of the JDBC driver's main class.
     * @return The full path of the JDBC driver class to be loaded
     */
    String getDriverClassName();
    
    /**
     * Given a database server (assumed to be compatible with this driver descriptor, or it will throw 
     * IllegalArgumentException), format a JDBC URL that can be used to connect to the server. This should be
     * implemented so that is reads the various properties from the server object such as hostname and port and uses
     * this to assemble the JDBC URL that should be used to connect to this database server with this driver.
     *
     * @param databaseServer Database server to connect to
     * @return JDBC url formatted for this driver
     * @throws IllegalArgumentException If the databaseServer is not supported by this driver descriptor
     */
    String formatJDBCUrl(DatabaseServer databaseServer);

    /**
     * Creates a connection factory specially designed for connecting to the server using this driver. This generally
     * returns a specialized implementation of {@code DatabaseConnectionFactory} that exposes extra methods special for
     * this driver.
     * @param databaseServer Database server to pre-initialize the connection factory for
     * @return Connection factory that can be used to establish a connection to the given database using this driver
     */
    T createDatabaseConnectionFactory(DatabaseServer databaseServer);
}
