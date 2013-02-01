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

import com.googlecode.jdbw.metadata.ServerMetaData;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * This interface represents a type (MySQL, PostgreSQL, etc...) of database,
 * with methods used for helping you adapt to this particular server's dialect
 * of SQL
 * @see DatabaseServerTypes
 * @author Martin Berglund
 */
public interface DatabaseServerType {
    /**
     * @return Name of the database server type
     */
    String getName();
    
    /**
     * @return SQLDialect for the server type
     */
    SQLDialect getSQLDialect();
        
    /**
     * Creates an SQLExecutor for this server type
     * @param connection Connection to be used by the SQLExecutor
     * @return SQLExecutor-implementation for this server type
     */
    SQLExecutor createExecutor(Connection connection);
    
    /**
     * Creates a MetaDataResolver for this server type
     * @param dataSource DataSource to be used by this MetaDataResolver
     * @return MetaDataResolver-implementation for this server type
     */
    ServerMetaData createMetaDataResolver(DataSource dataSource);   
    
    /**
     * Check an SQLException with this server type if it is considered a connection error
     * @param e SQLException to check
     * @return true if the SQLException is caused by connection problems, false otherwise
     */
    boolean isConnectionError(SQLException e);
}
