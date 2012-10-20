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

import com.googlecode.jdbw.metadata.DefaultServerMetaData;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.metadata.Schema;
import com.googlecode.jdbw.metadata.Table;
import com.googlecode.jdbw.util.MockResultSet;
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
    protected ResultSet getStoredProcedureMetadata(Connection pooledConnection, Schema schema, String procedureName) throws SQLException {
        SQLWorker worker = new SQLWorker(new SybaseExecutor(pooledConnection));
        List<Object[]> rows = new ArrayList<Object[]>();
        for(String foundProcedureName: worker.leftColumnAsString(
                "SELECT name FROM " + schema.getCatalog().getName() + "..sysobjects WHERE type = 'P' ORDER BY name ASC")) {
            
            if(procedureName == null || procedureName.equals(foundProcedureName)) {
                rows.add(new Object[] { null, null, procedureName });
            }
        }
        return new MockResultSet(rows);
    }
/*
    @Override
    public String getStoredProcedureCode(String catalogName, String schemaName, String procedureName) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        final AtomicInteger counter = new AtomicInteger(2);
        DatabaseConnection tempConnection = new DatabaseConnectionImpl(dataSource, null, DatabaseServerTypes.SYBASE_ASE);
        DatabaseTransaction transaction = tempConnection.beginTransaction(TransactionIsolation.READ_COMMITTED);
        transaction.execute(new ExecuteResultHandlerAdapter(), "use " + catalogName);
        transaction.execute(new ExecuteResultHandlerAdapter() {
            @Override
            public boolean nextResultSet() {
                counter.decrementAndGet();
                return true;
            }

            @Override
            public boolean nextRow(Object[] row) {
                if(counter.get() > 0) {
                    return true;
                }
                sb.append((String) row[0]);
                return true;
            }
        }, "sp_helptext " + procedureName);
        transaction.execute(new ExecuteResultHandlerAdapter(), "use " + tempConnection.getDefaultCatalogName());
        transaction.rollback();
        return sb.toString();
    }
*/

    @Override
    public List<Index> getIndexes(Table table) throws SQLException {
        Connection pooledConnection = dataSource.getConnection();
        
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
            if(indexName == null || "".equals(indexName) || clusteredIndexes.contains(indexName))
                continue;
            
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
                if(result.containsKey(indexName)) {
                    result.get(indexName).addColumn(table.getColumn(columnName));
                }
                else {
                    result.put(indexName, 
                            createIndex(
                                table, 
                                indexName,
                                clusteredIndexes.contains(indexName) ? DatabaseMetaData.tableIndexClustered : 0,
                                resultSet.getBoolean("NON_UNIQUE"),
                                table.getColumn(columnName)));
                }
            }
            return sortIndexList(new ArrayList<Index>(result.values()));
        } finally {
            pooledConnection.close();
        }
    }
}
