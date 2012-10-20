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
package com.googlecode.jdbw.metadata;

import java.sql.SQLException;
import java.util.List;

public interface ServerMetaData {
    
    List<Catalog> getCatalogs() throws SQLException;
    
    Catalog getCatalog(String catalogName) throws SQLException;
    
    List<Schema> getSchemas(Catalog catalog) throws SQLException;
    
    Schema getSchema(Catalog catalog, String schemaName) throws SQLException;
    
    List<Table> getTables(Schema schema) throws SQLException;
    
    Table getTable(Schema schema, String tableName) throws SQLException;
    
    List<SystemTable> getSystemTables(Schema schema) throws SQLException;
    
    SystemTable getSystemTable(Schema schema, String systemTableName) throws SQLException;
    
    List<TableColumn> getColumns(Table table) throws SQLException;
    
    List<Index> getIndexes(Table table) throws SQLException;
    
    List<View> getViews(Schema schema) throws SQLException;
    
    View getView(Schema schema, String viewName) throws SQLException;
    
    List<ViewColumn> getColumns(View view) throws SQLException;
    
    List<StoredProcedure> getStoredProcedures(Schema schema) throws SQLException;
    
    StoredProcedure getStoredProcedure(Schema schema, String procedureName) throws SQLException;
    
    List<Function> getFunctions(Schema schema) throws SQLException;
    
    Function getFunction(Schema schema, String functionName) throws SQLException;
}
