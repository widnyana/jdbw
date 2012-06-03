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
 * Copyright (C) 2009-2012 mabe02
 */
package com.googlecode.jdbw;

import com.googlecode.jdbw.server.DatabaseServerTraits;
import java.sql.SQLException;

/**
 * This interface represents a type ('MySQL', 'PostgreSQL', etc) of database,
 * with methods used for helping you adapt to this particular server's dialect
 * of SQL
 * @see DatabaseServerTypes
 * @author mabe02
 */
public interface DatabaseServerType {
    /**
     * @return Name of the database server type
     */
    String getName();
    
    /**
     * @return Traits of the database server type
     */
    DatabaseServerTraits getTraits();
    
    /**
     * @return Factory to be used for creating special implementations of JDBW classes
     */
    JDBWObjectFactory getJDBWObjectFactory();
    
    /**
     * Check an SQLException with this server type if it is considered a connection error
     * @param e SQLException to check
     * @return true if the SQLException is caused by connection problems, false otherwise
     */
    boolean isConnectionError(SQLException e);
}
