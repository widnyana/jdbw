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

import com.googlecode.jdbw.metadata.Catalog;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
public interface DatabaseConnection {
    
    /**
     * @return Type of the server this connection is connected to, or null if unknown
     */
    DatabaseServerType getServerType();
    
    /**
     * Allocated a connection and begins a transaction
     * @param isolation Isolation level for the new transaction
     * @return DatabaseTransaction object representing the new transaction
     * @throws SQLException 
     */
    DatabaseTransaction beginTransaction(TransactionIsolation isolation) throws SQLException;
    
    /**
     * Creates an AutoExecutor using this database connection for supplying connections
     * @return AutoExecutor using this connection
     */
    AutoExecutor createAutoExecutor();

    /**
     * @return Default catalog name of this connection
     */
    String getDefaultCatalogName();
    
    /**
     * @return Default transaction isolation of this connection
     */
    TransactionIsolation getDefaultTransactionIsolation();
    
    /**
     * @return List of all catalogs available on this server
     * @throws SQLException 
     */
    List<Catalog> getCatalogs() throws SQLException;
    
    /**
     * Looks up one catalog on the server
     * @param catalogName Name of the catalog
     * @return Catalog object representing the catalog or null if not found
     * @throws SQLException 
     */
    Catalog getCatalog(String catalogName) throws SQLException;
    
    /**
     * Closes this connection and any underlying data source
     */
    void close();
}
