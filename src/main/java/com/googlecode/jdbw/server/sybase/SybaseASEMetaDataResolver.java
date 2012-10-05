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

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.DatabaseTransaction;
import com.googlecode.jdbw.TransactionIsolation;
import com.googlecode.jdbw.impl.DatabaseConnectionImpl;
import com.googlecode.jdbw.metadata.MetaDataResolver;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import com.googlecode.jdbw.util.SQLWorker;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned to Sybase ASE
 * @author Martin Berglund
 */
class SybaseASEMetaDataResolver extends MetaDataResolver {

    SybaseASEMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Map<String, Object> extractColumnFromMetaResult(ResultSet resultSet) throws SQLException {
        Map<String, Object> columnProperties = new HashMap<String, Object>(8);
        columnProperties.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
        columnProperties.put("DATA_TYPE", resultSet.getInt("DATA_TYPE"));
        columnProperties.put("TYPE_NAME", resultSet.getString("TYPE_NAME"));
        columnProperties.put("COLUMN_SIZE", resultSet.getInt("COLUMN_SIZE"));
        columnProperties.put("DECIMAL_DIGITS", resultSet.getInt("DECIMAL_DIGITS"));
        columnProperties.put("NULLABLE", resultSet.getInt("NULLABLE"));
        columnProperties.put("ORDINAL_POSITION", resultSet.getInt("ORDINAL_POSITION"));
        columnProperties.put("IS_AUTOINCREMENT", "");
        return columnProperties;
    }

    @Override
    protected List<String> getStoredProcedureNames(String catalogName, String schemaName) throws SQLException {
        DatabaseConnection tempConnection = new DatabaseConnectionImpl(dataSource, null, DatabaseServerTypes.SYBASE_ASE);
        SQLWorker worker = new SQLWorker(tempConnection.createAutoExecutor());
        return worker.leftColumnAsString("SELECT name FROM " + catalogName + "..sysobjects WHERE type = 'P'");
    }

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

    @Override
    protected List<Map<String, Object>> getIndexes(String catalogName, String schemaName, String tableName) throws SQLException {
        List<Map<String, Object>> indexes = super.getIndexes(catalogName, schemaName, tableName);
        SQLWorker worker = new SQLWorker(new DatabaseConnectionImpl(dataSource, null, DatabaseServerTypes.SYBASE_ASE).createAutoExecutor());
        List<Object[]> rows = worker.query("select i.name, i.status, i.status2 " + 
                "FROM " + catalogName + "." + schemaName + ".sysobjects o, " + 
                "     " + catalogName + "." + schemaName + ".sysindexes i " + 
                "WHERE o.name = ? AND o.type = 'U' AND " + 
                "         o.id = i.id", tableName);
        //Use a safer way of detecting clustered indexes
        indexLoop:
        for(Map<String, Object> indexDef : indexes) {
            for(Object[] row : rows) {
                if(row[0].toString().trim().equals(indexDef.get("INDEX_NAME"))) {
                    int status = (Integer) row[1];
                    int status2 = (Integer) row[2];
                    boolean clustered = (status & 16) > 0 || (status2 & 512) > 0;
                    indexDef.put("TYPE", DatabaseMetaData.tableIndexClustered);
                    continue indexLoop;
                }
            }
        }
        return indexes;
    }
    
}
