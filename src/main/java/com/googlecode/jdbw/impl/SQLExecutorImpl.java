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

package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.util.NullValue;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is an implementation of the {@code SQLExecutor} that provides
 * most of the functionality required for sending queries and receiving data.
 * This class does not deal with setting up and tearing down database 
 * connections; rather it expects the user to pass in a valid connection and
 * close it when done.
 * 
 * <p>Normally, you wouldn't use this class directly, but rather through 
 * classes such as the {@code AutoExecutor} or the {@code DatabaseTransaction},
 * which is using this class behind the scenes. You call methods on  
 * {@code DatabaseConnection} to get one of those.
 * 
 * @see AutoExecutor
 * @see DatabaseTransaction
 * @see DatabaseConnection
 * @author Martin Berglund
 */
public abstract class SQLExecutorImpl implements SQLExecutor
{
    protected final Connection connection;

    protected SQLExecutorImpl(Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException
    {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = prepareExecuteStatement(SQL);
            for(int i = 0; i < parameters.length; i++)
                setParameter(statement, parameters[i], i + 1);

            statement.setMaxRows(handler.getMaxRowsToFetch());
            statement.execute();
            
            if(isInsertSQL(SQL)) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                while(generatedKeys.next()) {
                    handler.onGeneratedKey(generatedKeys.getObject(1));
                }
                generatedKeys.close();
            }

            while(true) {
                resultSet = statement.getResultSet();
                if(resultSet != null) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    List<String> columnNames = new ArrayList<String>();
                    for(int i = 0; i < resultSetMetaData.getColumnCount(); i++)
                        columnNames.add(resultSetMetaData.getColumnLabel(i + 1));

                    List<Integer> columnTypes = new ArrayList<Integer>();
                    for(int i = 0; i < resultSetMetaData.getColumnCount(); i++)
                        columnTypes.add(resultSetMetaData.getColumnType(i + 1));
                    
                    handler.onResultSet(columnNames, columnTypes);

                    SQLWarning warning = resultSet.getWarnings();
                    if(warning != null)
                        handler.onWarning(warning);

                    boolean gotCancel = false;
                    while(resultSet.next() && !gotCancel) {
                        Object []row = new Object[resultSetMetaData.getColumnCount()];
                        for(int i = 0; i < row.length; i++)
                            row[i] = resultSet.getObject(i + 1);
                        if(!handler.nextRow(row))
                            gotCancel = true;
                    }
                }
                else {
                    int updateCount = statement.getUpdateCount();
                    if(updateCount == -1)
                        break;
                    else
                        handler.onUpdateCount(updateCount);
                }
                
                if(statement.getMoreResults())
                    if(!handler.nextResultSet())
                        break;
            }
            handler.onDone();
        }
        finally {
            if(resultSet != null)
                try { resultSet.close(); } catch(SQLException e) {}
            if(statement != null)
                try { statement.close(); } catch(SQLException e) {}
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException
    {
        PreparedStatement statement = null;
        try {
            statement = prepareBatchUpdateStatement(SQL);

            for(Object []row: parameters) {
                for(int i = 0; i < row.length; i++)
                    setParameter(statement, row[i], i + 1);
                statement.addBatch();
            }

            int []batchResult = statement.executeBatch();
            handler.onBatchResult(batchResult);

            SQLWarning warning = statement.getWarnings();
            if(warning != null)
                handler.onWarning(warning);
        }
        finally {
            if(statement != null)
                try { statement.close(); } catch(SQLException e) {}
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException
    {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for(String row: batchedSQL)
                statement.addBatch(row);

            int []batchResult = statement.executeBatch();
            handler.onBatchResult(Arrays.copyOf(batchResult, batchResult.length));

            SQLWarning warning = statement.getWarnings();
            if(warning != null)
                handler.onWarning(warning);
        }
        finally {
            if(statement != null)
                try { statement.close(); } catch(SQLException e) {}
        }
    }

    //   -- INFO --
    // Protected methods below are for sub-classes tuned for particular database
    // servers which may or may not support all of JDBC. Please see 
    // MySQLExecutor for an example.
    
    protected PreparedStatement prepareQueryStatement(String SQL) throws SQLException
    {
        return connection.prepareStatement(SQL);
    }

    protected PreparedStatement prepareInsertStatement(String SQL) throws SQLException
    {
        return connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
    }

    protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException
    {
        return connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
    }

    protected PreparedStatement prepareExecuteStatement(String SQL) throws SQLException
    {
        if(isInsertSQL(SQL))
            return prepareInsertStatement(SQL);
        else
            return connection.prepareStatement(SQL);
    }

    protected void executeUpdate(Statement statement, String SQL) throws SQLException
    {
        statement.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
    }

    protected ResultSet getGeneratedKeys(Statement statement) throws SQLException
    {
        return statement.getGeneratedKeys();
    }

    protected void setParameter(PreparedStatement statement, Object object, int i) throws SQLException
    {
        if(object == null)
            statement.setNull(i, java.sql.Types.VARCHAR);
        else if(object instanceof NullValue.Binary)
            statement.setNull(i, java.sql.Types.BINARY);
        else if(object instanceof NullValue.Decimal)
            statement.setNull(i, java.sql.Types.DECIMAL);
        else if(object instanceof NullValue.Double)
            statement.setNull(i, java.sql.Types.DOUBLE);
        else if(object instanceof NullValue.Integer)
            statement.setNull(i, java.sql.Types.INTEGER);
        else if(object instanceof NullValue.String)
            statement.setNull(i, java.sql.Types.VARCHAR);
        else if(object instanceof NullValue.Timestamp)
            statement.setNull(i, java.sql.Types.TIMESTAMP);
        else if(object instanceof String)
            statement.setString(i, (String)object);
        else if(object instanceof Byte)
            statement.setByte(i, (Byte)object);
        else if(object instanceof Short)
            statement.setShort(i, (Short)object);
        else if(object instanceof Integer)
            statement.setInt(i, (Integer)object);
        else if(object instanceof Long)
            statement.setLong(i, (Long)object);
        else if(object instanceof BigDecimal)
            statement.setBigDecimal(i, (BigDecimal)object);
        else if(object instanceof Date)
            statement.setDate(i, (Date)object);
        else if(object instanceof Double)
            statement.setDouble(i, (Double)object);
        else if(object instanceof Float)
            statement.setFloat(i, (Float)object);
        else if(object instanceof Boolean)
            statement.setBoolean(i, (Boolean)object);
        else if(object instanceof Timestamp)
            statement.setTimestamp(i, (Timestamp)object);
        else if(object instanceof java.util.Date)
            statement.setTimestamp(i, new Timestamp(((java.util.Date)object).getTime()));
        else
            statement.setObject(i, object);
    }

    private boolean isInsertSQL(String SQL) {
        return SQL.trim().substring(0, "insert".length()).toLowerCase().equals("insert");
    }
}
