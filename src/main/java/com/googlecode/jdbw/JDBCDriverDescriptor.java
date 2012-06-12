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

import com.googlecode.jdbw.server.mysql.MySQLCommonJDBCDriverDescriptor;

/**
 * This interface describes a JDBC driver and how to use it. You'll need to
 * implement this interface in order to use a custom JDBC driver not bundled
 * with jdbw.
 * @see MySQLCommonJDBCDriverDescriptor
 * @author mabe02
 */
public interface JDBCDriverDescriptor {
    /**
     * @return The full path of the JDBC driver class to be loaded
     */
    String getDriverClassName();
    
    /**
     * Given a host, port and default catalog, return a JDBC URL formatted to
     * be used by this driver
     * @param host Hostname of the database server
     * @param port Port number to connect on
     * @param defaultCatalog Default catalog to use
     * @return JDBC url formatted for this driver
     */
    String formatJDBCUrl(String host, int port, String defaultCatalog);
}
