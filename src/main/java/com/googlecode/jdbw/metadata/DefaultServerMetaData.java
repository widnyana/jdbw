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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class DefaultServerMetaData implements ServerMetaData {
    
    protected final DataSource dataSource;

    public DefaultServerMetaData(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Catalog> getCatalogs() throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<Catalog> result = new ArrayList<Catalog>();
            for(String catalogName : readResultSetColumn(getCatalogNames(pooledConnection), 1)) {
                result.add(createCatalog(catalogName));
            }
            return result;
        }
        finally {
            pooledConnection.close();
        }
    }

    @Override
    public Catalog getCatalog(String catalogName) throws SQLException {
        for(Catalog catalog: getCatalogs()) {
            if(catalog.getName().equals(catalogName))
                return catalog;
        }
        return null;
    }

    @Override
    public List<Schema> getSchemas(Catalog catalog) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<Schema> result = new ArrayList<Schema>();
            ResultSet resultSet = getSchemaMetadata(pooledConnection, catalog, null);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            try {
                while(resultSet.next()) {
                    //Some database venders doesn't send the second column!
                    if(resultSetMetaData.getColumnCount() < 2) {
                        result.add(createSchema(catalog, resultSet.getString(1)));
                    }
                    else if(resultSet.getString(2) == null || resultSet.getString(2).equals(catalog.getName())) {
                        result.add(createSchema(catalog, resultSet.getString(1)));
                    }
                }
                return result;
            }
            finally {
                try {
                    resultSet.close();
                }
                catch(SQLException e2) {
                }
            }
        }
        finally {
            pooledConnection.close();
        }
    }

    @Override
    public Schema getSchema(Catalog catalog, String schemaName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            ResultSet resultSet = getSchemaMetadata(pooledConnection, catalog, schemaName);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            try {
                while(resultSet.next()) {
                    //Some database venders doesn't send the second column!
                    if(resultSetMetaData.getColumnCount() < 2) {
                        if(resultSet.getString(1) != null && resultSet.getString(1).equals(schemaName)) {
                            return createSchema(catalog, resultSet.getString(1));
                        }
                    }
                    else if(resultSet.getString(2) == null || resultSet.getString(2).equals(catalog.getName())) {
                        if(resultSet.getString(1) != null && resultSet.getString(1).equals(schemaName)) {
                            return createSchema(catalog, resultSet.getString(1));
                        }
                    }
                }
                return null;
            }
            finally {
                try {
                    resultSet.close();
                }
                catch(SQLException e2) {
                }
            }
        }
        finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<Table> getTables(Schema schema) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<Table> result = new ArrayList<Table>();
            for(String tableName: readResultSetColumn(getTableMetadata(pooledConnection, schema, null), 3)) {
                result.add(createTable(schema, tableName));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public Table getTable(Schema schema, String tableName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            for(String foundTableName: readResultSetColumn(getTableMetadata(pooledConnection, schema, tableName), 3)) {
                if(foundTableName.equals(tableName)) {
                    return createTable(schema, foundTableName);
                }
            }
            return null;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<SystemTable> getSystemTables(Schema schema) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<SystemTable> result = new ArrayList<SystemTable>();
            for(String tableName: readResultSetColumn(getSystemTableMetadata(pooledConnection, schema, null), 3)) {
                result.add(createSystemTable(schema, tableName));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public SystemTable getSystemTable(Schema schema, String systemTableName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            for(String foundTableName: readResultSetColumn(getSystemTableMetadata(pooledConnection, schema, systemTableName), 3)) {
                if(foundTableName.equals(systemTableName)) {
                    return createSystemTable(schema, foundTableName);
                }
            }
            return null;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<View> getViews(Schema schema) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<View> result = new ArrayList<View>();
            for(String viewName: readResultSetColumn(getViewMetadata(pooledConnection, schema, null), 3)) {
                result.add(createView(schema, viewName));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public View getView(Schema schema, String viewName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            for(String foundViewName: readResultSetColumn(getViewMetadata(pooledConnection, schema, viewName), 3)) {
                if(foundViewName.equals(viewName)) {
                    return createView(schema, foundViewName);
                }
            }
            return null;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<TableColumn> getColumns(Table table) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<TableColumn> result = new ArrayList<TableColumn>();
            ResultSet resultSet = getTableColumnMetadata(pooledConnection, table);
            while(resultSet.next()) {
                result.add(
                        createTableColumn(
                            table,
                            resultSet.getInt("ORDINAL_POSITION"),
                            resultSet.getString("COLUMN_NAME"),
                            resultSet.getInt("DATA_TYPE"),
                            resultSet.getString("TYPE_NAME"),
                            resultSet.getInt("COLUMN_SIZE"),
                            resultSet.getInt("DECIMAL_DIGITS"),
                            resultSet.getInt("NULLABLE"),
                            resultSet.getString("IS_AUTOINCREMENT")));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<Index> getIndexes(Table table) throws SQLException {
        
        //Preload all the table columns so we don't need to look them up later, while the connection
        //below is in use (won't work for single-connection pools)
        Map<String, TableColumn> tableColumns = table.getColumnMap();
        
        Connection pooledConnection = dataSource.getConnection();
        try {
            Map<String, Index> result = new HashMap<String, Index>();
            ResultSet resultSet = getIndexMetadata(pooledConnection, table);
            while(resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if(result.containsKey(indexName)) {
                    result.get(indexName).addColumn(tableColumns.get(columnName));
                }
                else {
                    result.put(indexName, 
                            createIndex(
                                table, 
                                indexName,
                                resultSet.getShort("TYPE"),
                                !resultSet.getBoolean("NON_UNIQUE"),
                                tableColumns.get(columnName)));
                }
            }
            return sortIndexList(new ArrayList<Index>(result.values()));
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<ViewColumn> getColumns(View view) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<ViewColumn> result = new ArrayList<ViewColumn>();
            ResultSet resultSet = getViewColumnMetadata(pooledConnection, view);
            while(resultSet.next()) {
                result.add(
                        createViewColumn(
                            view,
                            resultSet.getInt("ORDINAL_POSITION"),
                            resultSet.getString("COLUMN_NAME"),
                            resultSet.getInt("DATA_TYPE"),
                            resultSet.getString("TYPE_NAME"),
                            resultSet.getInt("COLUMN_SIZE"),
                            resultSet.getInt("DECIMAL_DIGITS"),
                            resultSet.getInt("NULLABLE"),
                            resultSet.getString("IS_AUTOINCREMENT")));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<StoredProcedure> getStoredProcedures(Schema schema) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<StoredProcedure> result = new ArrayList<StoredProcedure>();
            for(String procedureName: readResultSetColumn(getStoredProcedureMetadata(pooledConnection, schema, null), 3)) {
                result.add(createStoredProcedure(schema, procedureName));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public StoredProcedure getStoredProcedure(Schema schema, String procedureName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            for(String foundProcName: readResultSetColumn(getStoredProcedureMetadata(pooledConnection, schema, procedureName), 3)) {
                if(foundProcName.equals(procedureName)) {
                    return createStoredProcedure(schema, foundProcName);
                }
            }
            return null;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public List<Function> getFunctions(Schema schema) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            List<Function> result = new ArrayList<Function>();
            for(String functionName: readResultSetColumn(getFunctionMetadata(pooledConnection, schema, null), 3)) {
                result.add(createFunction(schema, functionName));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    public Function getFunction(Schema schema, String functionName) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        try {
            for(String foundFunctionName: readResultSetColumn(getFunctionMetadata(pooledConnection, schema, functionName), 3)) {
                if(foundFunctionName.equals(functionName)) {
                    return createFunction(schema, foundFunctionName);
                }
            }
            return null;
        } finally {
            pooledConnection.close();
        }
    }
    
    protected List<String> readResultSetColumn(ResultSet resultSet, int index) throws SQLException {
        List<String> list = new ArrayList<String>();
        try {
            while(resultSet.next()) {
                list.add(resultSet.getString(index));
            }
            return list;
        }
        finally {
            try {
                resultSet.close();
            }
            catch(SQLException e) {
            }
        }
    }
    
    protected Catalog createCatalog(String catalogName) {
        return new Catalog(this, catalogName);
    }
    
    protected Schema createSchema(Catalog catalog, String schemaName) {
        return new Schema(this, catalog, schemaName);
    }
    
    protected Table createTable(Schema schema, String tableName) {
        return new Table(this, schema, tableName);
    }

    private SystemTable createSystemTable(Schema schema, String tableName) {
        return new SystemTable(this, schema, tableName);
    }
    
    protected View createView(Schema schema, String viewName) {
        return new View(this, schema, viewName);
    }
    
    protected StoredProcedure createStoredProcedure(Schema schema, String procedureName) {
        return new StoredProcedure(this, schema, procedureName);
    }

    protected Function createFunction(Schema schema, String functionName) {
        return new Function(schema, functionName);
    }

    protected TableColumn createTableColumn(
            Table table, 
            int ordinalPosition, 
            String columnName, 
            int sqlType, 
            String typeName, 
            int columnSize, 
            int decimalDigits, 
            int nullable, 
            String autoIncrement) {
        
        return new TableColumn(table, ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
    }

    protected ViewColumn createViewColumn(
            View view, 
            int ordinalPosition, 
            String columnName, 
            int sqlType, 
            String typeName, 
            int columnSize, 
            int decimalDigits, 
            int nullable, 
            String autoIncrement) {
        
        return new ViewColumn(view, ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
    }

    protected Index createIndex(
            Table table, 
            String indexName, 
            short type, 
            boolean unique, 
            TableColumn firstColumn) {
        
        boolean clustered = (type == DatabaseMetaData.tableIndexClustered);
        boolean primaryKey = unique && clustered;
        return new Index(table, indexName, unique, clustered, primaryKey, firstColumn);
    }

    protected ResultSet getCatalogNames(Connection pooledConnection) throws SQLException {
        return pooledConnection.getMetaData().getCatalogs();
    }

    protected ResultSet getSchemaMetadata(Connection pooledConnection, Catalog catalog, String schemaName) throws SQLException {
        return pooledConnection.getMetaData().getSchemas(catalog != null ? catalog.getName() : null, schemaName);
    }

    protected ResultSet getTableMetadata(Connection pooledConnection, Schema schema, String tableName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                tableName, 
                new String[]{"TABLE"});
    }

    protected ResultSet getSystemTableMetadata(Connection pooledConnection, Schema schema, String systemTableName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                systemTableName, 
                new String[]{"SYSTEM TABLE"});
    }

    protected ResultSet getViewMetadata(Connection pooledConnection, Schema schema, String viewName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                viewName, 
                new String[]{"VIEW"});
    }

    protected ResultSet getIndexMetadata(Connection pooledConnection, Table table) throws SQLException {
        return pooledConnection.getMetaData().getIndexInfo(
                table.getSchema().getCatalog().getName(), 
                table.getSchema().getName(), 
                table.getName(), 
                false /* unique */,
                false /* approximate */);
    }

    protected ResultSet getTableColumnMetadata(Connection pooledConnection, Table table) throws SQLException {
        return pooledConnection.getMetaData().getColumns(
                table.getSchema().getCatalog().getName(), 
                table.getSchema().getName(), 
                table.getName(), 
                null /* columnNamePattern */);
    }

    protected ResultSet getViewColumnMetadata(Connection pooledConnection, View view) throws SQLException {
        return pooledConnection.getMetaData().getColumns(
                view.getSchema().getCatalog().getName(), 
                view.getSchema().getName(), 
                view.getName(), 
                null /* columnNamePattern */);
    }

    protected ResultSet getStoredProcedureMetadata(Connection pooledConnection, Schema schema, String procedureName) throws SQLException {
        return pooledConnection.getMetaData().getProcedures(
                schema.getCatalog().getName(), 
                schema.getName(), 
                procedureName);
    }

    protected ResultSet getFunctionMetadata(Connection pooledConnection, Schema schema, String functionName) throws SQLException {
        return pooledConnection.getMetaData().getFunctions(
                schema.getCatalog().getName(), 
                schema.getName(), 
                functionName);
    }

    protected List<Index> sortIndexList(List<Index> indexes) {
        Collections.sort(indexes);
        return indexes;
    }
}
