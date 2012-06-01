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

package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.metadata.DefaultMetaDataFactory;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author mabe02
 */
public class DefaultMetaDataResolver extends MetaDataResolver
{
    protected final DefaultDatabaseConnection connectionPool;

    protected DefaultMetaDataResolver(DefaultDatabaseConnection connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    @Override
    protected MetaDataFactory getMetaDataFactory()
    {
        return new DefaultMetaDataFactory(this);
    }

    @Override
    protected List<String> getCatalogNames() throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        try {
            return readResultSetColumn(databaseMetaData.getCatalogs(), 1);
        }
        finally {
            pooledConnection.done();
        }
    }

    @Override
    protected List<String> getSchemaNames(String catalogName) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        try {
            List<String> resultList = new ArrayList<String>();
            ResultSet resultSet = databaseMetaData.getSchemas();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            try {
                while(resultSet.next()) {
                    //Some database venders doesn't send the second column!
                    if(resultSetMetaData.getColumnCount() < 2)
                        resultList.add(resultSet.getString(1));
                    else if(resultSet.getString(2) == null || resultSet.getString(2).equals(catalogName))
                        resultList.add(resultSet.getString(1));
                }
                return resultList;
            }
            finally {
                try {
                    resultSet.close();
                }
                catch(SQLException e2) {}
            }
        }
        finally {
            pooledConnection.done();
        }
    }

    @Override
    protected List<String> getUserTableNames(String catalogName, String schemaName) throws SQLException
    {
        return getTableNames(catalogName, schemaName, "TABLE");
    }

    @Override
    protected List<String> getSystemTableNames(String catalogName, String schemaName) throws SQLException
    {
        return getTableNames(catalogName, schemaName, "SYSTEM TABLE");
    }

    @Override
    protected List<String> getViewNames(String catalogName, String schemaName) throws SQLException
    {
        return getTableNames(catalogName, schemaName, "VIEW");
    }

    private List<String> getTableNames(String catalogName, String schemaName, String tableType) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        try {
            return readResultSetColumn(databaseMetaData.getTables(catalogName, schemaName, null, new String [] { tableType }), 3);
        }
        finally {
            pooledConnection.done();
        }
    }

    @Override
    protected List<String> getStoredProcedureNames(String catalogName, String schemaName) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        try {
            return readResultSetColumn(databaseMetaData.getProcedures(catalogName, schemaName, null), 3);
        }
        finally {
            pooledConnection.done();
        }
    }

    @Override
    protected List<String> getFunctionNames(String catalogName, String schemaName) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        try {
            return readResultSetColumn(databaseMetaData.getFunctions(catalogName, schemaName, null), 3);
        }
        finally {
            pooledConnection.done();
        }
    }

    @Override
    protected List<Column> getColumns(String catalogName, String schemaName, Table table) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        List<Column> columns = new ArrayList<Column>();
        try {
            ResultSet resultSet = databaseMetaData.getColumns(catalogName, schemaName, table.getName(), null);
            while(resultSet.next()) {
                Column column = extractColumnFromMetaResult(resultSet, table);
                columns.add(column);
            }
        }
        finally {
            pooledConnection.done();
        }
        Collections.sort(columns);
        return columns;
    }

    @Override
    protected List<Index> getIndexes(String catalogName, String schemaName, Table table) throws SQLException
    {
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();
        DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
        Map<String, Index> indexMap = new HashMap<String, Index>();
        try {
            ResultSet resultSet = databaseMetaData.getIndexInfo(catalogName, schemaName, table.getName(), false, false);
            while(resultSet.next())
                extractIndexDataFromMetaResult(resultSet, indexMap, table);
        }
        finally {
            pooledConnection.done();
        }
        List<Index> indexes = new ArrayList<Index>(indexMap.values());
        Collections.sort(indexes);
        return indexes;
    }

    @Override
    protected List<String> getProcedureInputParameterNames(String catalogName, String schemaName, StoredProcedure procedure) throws SQLException {
        
        PooledDatabaseConnection pooledConnection = connectionPool.getPooledConnection();try {
            DatabaseMetaData databaseMetaData = pooledConnection.getConnection().getMetaData();
            return readResultSetColumn(databaseMetaData.getProcedureColumns(catalogName, schemaName, procedure.getName(), null), 4);
        }
        finally {
            pooledConnection.done();
        }
    }

    protected Column extractColumnFromMetaResult(ResultSet resultSet, Table table) throws SQLException
    {
        String columnName = resultSet.getString("COLUMN_NAME");
        int sqlType = resultSet.getInt("DATA_TYPE");
        String typeName = resultSet.getString("TYPE_NAME");
        int columnSize = resultSet.getInt("COLUMN_SIZE");
        int decimalDigits = resultSet.getInt("DECIMAL_DIGITS");
        int nullable = resultSet.getInt("NULLABLE");
        int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
        String isAutoIncrement = resultSet.getString("IS_AUTOINCREMENT");
        Column column = new Column(ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, isAutoIncrement, table);
        return column;
    }

    protected void extractIndexDataFromMetaResult(ResultSet resultSet, Map<String, Index> indexMap, Table table) throws SQLException
    {
        String indexName = resultSet.getString("INDEX_NAME");
        boolean unique = !resultSet.getBoolean("NON_UNIQUE");
        boolean clustered = resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexClustered;
        String columnName = resultSet.getString("COLUMN_NAME");
        boolean primaryKey = unique && clustered;

        Column column = table.getColumn(columnName);

        if(indexName == null || column == null)   //Only named indexes and existing columns
            return;

        if(indexMap.containsKey(indexName))
            indexMap.get(indexName).addColumn(column);
        else
            indexMap.put(indexName, new Index(indexName, unique, clustered, primaryKey, table, column));
    }

    protected List<String> readResultSetColumn(ResultSet resultSet, int index) throws SQLException
    {
        List<String> list = new ArrayList<String>();
        try {
            while(resultSet.next())
                list.add(resultSet.getString(index));
            return list;
        }
        finally {
            try {
                resultSet.close();
            }
            catch(SQLException e) {}
        }
    }
}
