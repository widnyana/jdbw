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
 * Copyright (C) 2007-2012 mabe02
 */

package com.googlecode.jdbw.server.sybase;

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.DatabaseTransaction;
import com.googlecode.jdbw.TransactionIsolation;
import com.googlecode.jdbw.impl.DatabaseConnectionImpl;
import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.metadata.MetaDataResolver;
import com.googlecode.jdbw.metadata.Table;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import com.googlecode.jdbw.util.SQLWorker;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

/**
 *
 * @author mabe02
 */
class SybaseASEMetaDataResolver extends MetaDataResolver {

    SybaseASEMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Column extractColumnFromMetaResult(ResultSet resultSet, Table table) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        int sqlType = resultSet.getInt("DATA_TYPE");
        String typeName = resultSet.getString("TYPE_NAME");
        int columnSize = resultSet.getInt("COLUMN_SIZE");
        int decimalDigits = resultSet.getInt("DECIMAL_DIGITS");
        int nullable = resultSet.getInt("NULLABLE");
        int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
        String isAutoIncrement = "";
        Column column = new Column(ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, isAutoIncrement, table);
        return column;
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
    protected List<Index> getIndexes(String catalogName, String schemaName, Table table) throws SQLException {
        List<Index> indexes = super.getIndexes(catalogName, schemaName, table);
        List<Index> newIndexList = new ArrayList<Index>(indexes.size());
        SQLWorker worker = new SQLWorker(new DatabaseConnectionImpl(dataSource, null, DatabaseServerTypes.SYBASE_ASE).createAutoExecutor());
        List<Object[]> rows = worker.query("select i.name, i.status, i.status2 " + "FROM " + catalogName + "." + schemaName + ".sysobjects o, " + "     " + catalogName + "." + schemaName + ".sysindexes i " + "WHERE o.name = ? AND o.type = 'U' AND " + "         o.id = i.id", table.getName());
        //Use a safer way of detecting clustered indexes
        indexLoop:
        for(Index index : indexes) {
            for(Object[] row : rows) {
                if(row[0].toString().trim().equals(index.getName())) {
                    int status = (Integer) row[1];
                    int status2 = (Integer) row[2];
                    boolean clustered = (status & 16) > 0 || (status2 & 512) > 0;
                    Index newIndex = new Index(index.getName(), index.isUnique(), clustered, index.isUnique() && clustered, table, index.getColumns().get(0));
                    for(int i = 1; i < index.getColumnNames().size(); i++) {
                        newIndex.addColumn(index.getColumns().get(i));
                    }
                    newIndexList.add(newIndex);
                    continue indexLoop;
                }
            }
            newIndexList.add(index);
        }
        return newIndexList;
    }
    
}
