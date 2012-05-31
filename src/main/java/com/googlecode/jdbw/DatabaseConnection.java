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
import com.googlecode.jdbw.server.DatabaseServerTraits;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
public interface DatabaseConnection {
    DatabaseServerType getServerType();
    DatabaseServerTraits getTraits();

    DatabaseTransaction beginTransaction(TransactionIsolation isolation);
    SQLExecutor createAutoExecutor();

    String getDefaultCatalogName();
    TransactionIsolation getDefaultTransactionIsolation();
    
    List<Catalog> getCatalogs() throws SQLException;
    Catalog getCatalog(String catalogName) throws SQLException;
    
    void close();
}
