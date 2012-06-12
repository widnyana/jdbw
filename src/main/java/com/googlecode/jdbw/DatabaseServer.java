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

import java.util.Properties;

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
     * Created a new DataSource to this server and returns it wrapped in a 
     * DatabaseConnection
     * @param dataSourceFactory Factory to use when creating the DataSource
     * @return DatabaseConnection connected to the server
     */
    DatabaseConnection connect(DataSourceCreator dataSourceFactory);
    
    /**
     * @return Extra properties to use when connecting to this server
     */
    Properties getConnectionProperties();
}
