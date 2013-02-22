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
 *
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
    DatabaseConnection connect(DataSourceFactory dataSourceFactory);
}