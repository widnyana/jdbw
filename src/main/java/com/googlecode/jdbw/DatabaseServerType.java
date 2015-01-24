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
 * This interface represents a type (MySQL, PostgreSQL, etc...) of database, with methods used for helping you adapt to
 * this particular server's dialect of SQL and JDBC. All the database server supported by JDBW build-in have static
 * constants in {@code DatabaseServerTypes} that implements this interface.
 * @see DatabaseServerTypes
 * @author Martin Berglund
 */
public interface DatabaseServerType {
    /**
     * Returns a readable name of the database server type, such as "MySQL" and "Sybase ASE".
     * @return Name of the database server type
     */
    String getName();
    
    /**
     * Returns a dialect object for this database type that helps you create SQL that will work on this particular
     * server, without having to special case everything.
     * @return SQLDialect for the server type
     */
    SQLDialect getSQLDialect();
        
    /**
     * Creates an SQLExecutor for this server type, using a connection supplied. This method can be used if you have a
     * connection that could even be created from outside of JDBW and you want to create an SQLExecutor which is using
     * that connection for all its operations but tuned for this particular server type.
     * @param connection Connection to be used by the SQLExecutor
     * @return SQLExecutor-implementation for this server type
     */
    SQLExecutor createExecutor(Connection connection);
    
    /**
     * Creates a ServerMetaData for this server type, using a DataSource supplied. This ServerMetaData object will be
     * an implementation tuned for this particular database server and will probably not work with other servers.
     * @param dataSource DataSource to be used by this MetaDataResolver
     * @return MetaDataResolver-implementation for this server type
     */
    ServerMetaData createMetaDataResolver(DataSource dataSource);   
    
    /**
     * Check an SQLException with this server type if it is considered a connection error. Typically connection errors
     * would indicate that a query can be re-tried if the connection pool is able to re-establish connection to the
     * database server.
     * @param e SQLException to check if it's probably a connection error
     * @return true if the SQLException is caused by connection problems, false otherwise
     */
    boolean isConnectionError(SQLException e);
}
