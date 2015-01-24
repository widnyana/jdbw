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

/**
 * This interface defines methods for loading meta data from a database server and present it formatted in the data
 * model defined in this package
 */
public interface ServerMetaData {

    /**
     * Returns all catalogs on this database server
     * @return All catalogs on this database server
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<Catalog> getCatalogs() throws SQLException;

    /**
     * Returns a single catalog based on name
     * @param catalogName Name of the catalog to return
     * @return Catalog with the name specified or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    Catalog getCatalog(String catalogName) throws SQLException;

    /**
     * Lists all schemas in the catalog supplied
     * @param catalog Catalog to list schemas in
     * @return List of schemas in the catalog
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<Schema> getSchemas(Catalog catalog) throws SQLException;

    /**
     * Returns a particular schema selected by name in a catalog
     * @param catalog Catalog to select the schema from
     * @param schemaName Name of the schema to select out
     * @return Schema in the catalog with the specified name or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    Schema getSchema(Catalog catalog, String schemaName) throws SQLException;

    /**
     * Returns a list of tables in a particular schema
     * @param schema Schema to list tables in
     * @return List of tables in the specified schema
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<Table> getTables(Schema schema) throws SQLException;

    /**
     * Returns a particular table in a schema, based on name
     * @param schema Schema to select table from
     * @param tableName Name of the table to select
     * @return The table in the specified schema with the specified name, or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    Table getTable(Schema schema, String tableName) throws SQLException;

    /**
     * Returns a list of system tables in a particular schema
     * @param schema Schema to list tables in
     * @return List of system tables in the specified schema
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<SystemTable> getSystemTables(Schema schema) throws SQLException;

    /**
     * Returns a particular system table in a schema, based on name
     * @param schema Schema to select the system table from
     * @param systemTableName Name of the system table to select
     * @return The system table in the specified schema with the specified name, or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    SystemTable getSystemTable(Schema schema, String systemTableName) throws SQLException;

    /**
     * Returns a list of columns in a particular table
     * @param table Table to list columns in
     * @return List of columns in the specified table
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<TableColumn> getColumns(Table table) throws SQLException;

    /**
     * Returns a list of indexes in a particular table
     * @param table Table to list indexes in
     * @return List of indexes in the specified table
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<Index> getIndexes(Table table) throws SQLException;

    /**
     * Returns a list of views in a particular schema
     * @param schema Schema to list views in
     * @return List of views in the specified schema
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<View> getViews(Schema schema) throws SQLException;

    /**
     * Returns a particular view in a schema, based on name
     * @param schema Schema to select the view from
     * @param viewName Name of the view to select
     * @return The view in the specified schema with the specified name, or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    View getView(Schema schema, String viewName) throws SQLException;

    /**
     * Returns a list of columns in a particular view
     * @param view View to list columns in
     * @return List of columns in the specified view
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<ViewColumn> getColumns(View view) throws SQLException;

    /**
     * Returns a list of stored procedures in a particular schema
     * @param schema Schema to list stored procedures in
     * @return List of stored procedures in the specified schema
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<StoredProcedure> getStoredProcedures(Schema schema) throws SQLException;

    /**
     * Returns a particular stored procedure in a schema, based on name
     * @param schema Schema to select the stored procedure from
     * @param procedureName Name of the stored procedure to select
     * @return The stored procedure in the specified schema with the specified name, or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    StoredProcedure getStoredProcedure(Schema schema, String procedureName) throws SQLException;

    /**
     * Returns a list of functions in a particular schema
     * @param schema to list functions in
     * @return List of functions in the specified schema
     * @throws SQLException If there was an error while loading the information from the database
     */
    List<Function> getFunctions(Schema schema) throws SQLException;

    /**
     * Returns a particular function in a schema, based on name
     * @param schema Schema to select the function from
     * @param functionName Name of the function to select
     * @return The function in the specified schema with the specified name, or {@code null}
     * @throws SQLException If there was an error while loading the information from the database
     */
    Function getFunction(Schema schema, String functionName) throws SQLException;
}
