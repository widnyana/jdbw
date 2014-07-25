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

import com.googlecode.jdbw.metadata.Catalog;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface represents a connection to a database server, although in 
 * reality this is probably more than one connection (probably a pool of them).
 * The main methods used to interact with the database server are:
 * <ul>
 * <li>{@code beginTransaction} will start an open transaction and keep a 
 * dedicated connection for this until you either rollback or commit. Please
 * remember that if you don't properly rollback or commit, the underlying data
 * source may starve out of connections and become unusable.
 * <li>{@code createAutoExecutor} will create an object that exposes methods for
 * sending SQL code to the database server, but will operate in auto commit 
 * mode, and only allocates a connection from the underlying data source when
 * required. You can't starve the data source using auto executors, they can't
 * even be closed as they are stateless.
 * <li>{@code getCatalog} will give you access to the meta data information on
 * the remote server. These objects loads data from the server as you use it
 * </ul>
 * @author Martin Berglund
 */
public interface DatabaseConnection {
    
    /**
     * @return Type of the server this connection is connected to
     */
    DatabaseServerType getServerType();
    
    /**
     * Allocated a connection and begins a transaction
     * @param isolation Isolation level for the new transaction
     * @return DatabaseTransaction object representing the new transaction
     * @throws SQLException If an error occurred when allocating a connection
     * for this transaction
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
     * @throws SQLException If an error occurred when asking the database
     * for catalog information
     */
    List<Catalog> getCatalogs() throws SQLException;
    
    /**
     * Looks up one catalog on the server
     * @param catalogName Name of the catalog
     * @return Catalog object representing the catalog or null if not found
     * @throws SQLException If an error occurred when asking the database
     * for catalog information
     */
    Catalog getCatalog(String catalogName) throws SQLException;

    /**
     * Returns a Catalog object for ideally the currently used catalog, but
     * if unable to determine this will return the same as if calling
     * {@code getCatalog(getDefaultCatalogName())}
     * @return Catalog object representing the catalog or null if not found
     * @throws SQLException If an error occurred when asking the database
     * for catalog information
     */
    Catalog getCurrentCatalog() throws SQLException;
    
    /**
     * Closes this connection and any underlying data source
     */
    void close();
}
