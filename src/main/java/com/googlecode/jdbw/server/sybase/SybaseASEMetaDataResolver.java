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

package com.googlecode.jdbw.server.sybase;

import com.googlecode.jdbw.metadata.Catalog;
import com.googlecode.jdbw.metadata.DefaultServerMetaData;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.metadata.Schema;
import com.googlecode.jdbw.metadata.Table;
import com.googlecode.jdbw.metadata.TableColumn;
import com.googlecode.jdbw.util.SimpleResultSet;
import com.googlecode.jdbw.util.SQLWorker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned to Sybase ASE
 * @author Martin Berglund
 */
class SybaseASEMetaDataResolver extends DefaultServerMetaData {

    SybaseASEMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    protected ResultSet getSchemaMetadata(Connection pooledConnection, Catalog catalog, String schemaName) throws SQLException {
        return pooledConnection.getMetaData().getSchemas();
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
                            null));
            }
            return result;
        } finally {
            pooledConnection.close();
        }
    }

    @Override
    protected ResultSet getStoredProcedureMetadata(Connection pooledConnection, Schema schema, String procedureName) throws SQLException {
        SQLWorker worker = new SQLWorker(new SybaseExecutor(pooledConnection));
        List<Object[]> rows = new ArrayList<Object[]>();
        for(String foundProcedureName: worker.leftColumnAsString(
                "SELECT name FROM " + schema.getCatalog().getName() + "..sysobjects WHERE type = 'P' ORDER BY name ASC")) {
            
            if(procedureName == null || procedureName.equals(foundProcedureName)) {
                rows.add(new Object[] { null, null, procedureName });
            }
        }
        return new SimpleResultSet(rows);
    }

    @Override
    public List<Index> getIndexes(Table table) throws SQLException {
        //Preload all the table columns so we don't need to look them up later, while the connection
        //below is in use (won't work for single-connection pools)
        Map<String, TableColumn> tableColumns = table.getColumnMap();
        
        Connection pooledConnection = dataSource.getConnection();
        pooledConnection.setAutoCommit(true);
        SQLWorker worker = new SQLWorker(new SybaseExecutor(pooledConnection));
        String catalogName = table.getSchema().getCatalog().getName();
        String schemaName = table.getSchema().getName();
        List<Object[]> rows = worker.query("select i.name, i.status, i.status2 " + 
                "FROM " + catalogName + "." + schemaName + ".sysobjects o, " + 
                "     " + catalogName + "." + schemaName + ".sysindexes i " + 
                "WHERE o.name = ? AND o.type = 'U' AND " + 
                "         o.id = i.id", table.getName());
        Set<String> clusteredIndexes = new HashSet<String>();
        for(Object[] row: rows) {
            String indexName = (row[0] != null ? row[0].toString().trim() : null);
            if(indexName == null || "".equals(indexName) || clusteredIndexes.contains(indexName)) {
                continue;
            }
            
            int status = (Integer) row[1];
            int status2 = (Integer) row[2];
            boolean clustered = (status & 16) > 0 || (status2 & 512) > 0;
            if(clustered) {
                clusteredIndexes.add(indexName);
            }
        }
        
        try {
            Map<String, Index> result = new HashMap<String, Index>();
            ResultSet resultSet = getIndexMetadata(pooledConnection, table);
            while(resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                
                if(indexName == null) {
                    //Sybase seems to send a null row??
                }
                else if(result.containsKey(indexName)) {
                    result.get(indexName).addColumn(tableColumns.get(columnName));
                }
                else {
                    result.put(indexName, 
                            createIndex(
                                table, 
                                indexName,
                                clusteredIndexes.contains(indexName) ? DatabaseMetaData.tableIndexClustered : 0,
                                !resultSet.getBoolean("NON_UNIQUE"),
                                tableColumns.get(columnName)));
                }
            }
            return sortIndexList(new ArrayList<Index>(result.values()));
        } finally {
            pooledConnection.close();
        }
    }
}
