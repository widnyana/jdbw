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
import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import com.googlecode.jdbw.util.NullValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * This class is an implementation of the {@code SQLExecutor} that provides most of the functionality required for
 * sending queries and receiving data. This class does not deal with setting up and tearing down database connections;
 * rather it expects the user to pass in a valid connection and close it when done.
 * <p/>
 * <p/>Normally, you wouldn't use this class directly, but rather through classes such as the {@code AutoExecutor} or
 * the {@code DatabaseTransaction}, which is using this class behind the scenes. You call methods on
 * {@code DatabaseConnection} to get one of those.
 *
 * @author Martin Berglund
 * @see AutoExecutor
 * @see DatabaseTransaction
 * @see DatabaseConnection
 */
public abstract class SQLExecutorImpl implements SQLExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLExecutorImpl.class);

    protected final Connection connection;

    protected SQLExecutorImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(String SQL, Object... parameters) throws SQLException {
        execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException {
        execute(handler, 0, 0, SQL, parameters);
    }

    @Override
    public void execute(ExecuteResultHandler handler, int maxRowsToFetch, int queryTimeoutInSeconds, String SQL, Object... parameters) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = prepareExecuteStatement(SQL);
            for (int i = 0; i < parameters.length; i++) {
                setParameter(statement, parameters[i], i + 1);
            }

            setQueryTimeout(statement, queryTimeoutInSeconds);
            setMaxRowsToFetch(statement, maxRowsToFetch);
            execute(statement);

            if (canGetGeneratedKeys(SQL)) {
                ResultSet generatedKeys = getGeneratedKeys(statement);
                if (generatedKeys != null) {
                    while (generatedKeys.next()) {
                        handler.onGeneratedKey(generatedKeys.getObject(1));
                    }
                    generatedKeys.close();
                }
            }

            int resultSetCounter = 0;
            while (true) {
                int updateCount = getUpdateCount(statement);
                if (updateCount != -1) {
                    handler.onUpdateCount(updateCount);
                    statement.getMoreResults();
                    continue;
                }
                resultSet = getResultSet(statement);
                if (resultSet == null) {
                    if (statement.getMoreResults() == false) {
                        break;
                    }
                    continue;
                }

                boolean gotCancel = false;
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                if (!handler.onResultSet(new ResultSetInformationImpl(resultSetMetaData, resultSetCounter++))) {
                    gotCancel = true;
                }

                SQLWarning warning = getWarnings(resultSet);
                if (warning != null) {
                    handler.onWarning(warning);
                }

                while (resultSet.next() && !gotCancel) {
                    Object[] row = new Object[resultSetMetaData.getColumnCount()];
                    for (int i = 0; i < row.length; i++) {
                        row[i] = resultSet.getObject(i + 1);
                    }
                    if (!handler.nextRow(row)) {
                        gotCancel = true;
                    }
                }
                statement.getMoreResults();
            }
            handler.onDone();
        }
        finally {
            if (resultSet != null) {
                try {
                    close(resultSet);
                }
                catch (SQLException e) {
                    LOGGER.error("Unable to close result set after query", e);
                }
            }
            if (statement != null) {
                try {
                    close(statement);
                }
                catch (SQLException e) {
                    LOGGER.error("Unable to close statement after query", e);
                }
            }
        }
    }

    @Override
    public void batchWrite(String SQL, List<Object[]> parameters) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), SQL, parameters);
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareBatchUpdateStatement(SQL);

            for (Object[] row : parameters) {
                for (int i = 0; i < row.length; i++) {
                    setParameter(statement, row[i], i + 1);
                }
                addBatch(statement);
            }

            int[] batchResult = executeBatch(statement);
            handler.onBatchResult(batchResult);

            ResultSet generatedKeys = getGeneratedKeys(statement);
            if (generatedKeys != null) {
                while (generatedKeys.next()) {
                    handler.onGeneratedKey(generatedKeys.getObject(1));
                }
                generatedKeys.close();
            }

            SQLWarning warning = getWarnings(statement);
            if (warning != null) {
                handler.onWarning(warning);
            }
        }
        finally {
            if (statement != null) {
                try {
                    close(statement);
                }
                catch (SQLException e) {
                    LOGGER.error("Unable to close statement after batch write", e);
                }
            }
        }
    }

    @Override
    public void batchWrite(List<String> batchedSQL) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (String row : batchedSQL) {
                addBatch(statement, row);
            }

            int[] batchResult = executeBatch(statement);
            handler.onBatchResult(Arrays.copyOf(batchResult, batchResult.length));

            ResultSet generatedKeys = getGeneratedKeys(statement);
            if (generatedKeys != null) {
                while (generatedKeys.next()) {
                    handler.onGeneratedKey(generatedKeys.getObject(1));
                }
                generatedKeys.close();
            }

            SQLWarning warning = getWarnings(statement);
            if (warning != null) {
                handler.onWarning(warning);
            }
        }
        finally {
            if (statement != null) {
                try {
                    close(statement);
                }
                catch (SQLException e) {
                    LOGGER.error("Unable to close statement after batch write", e);
                }
            }
        }
    }

    //   -- INFO --
    // Protected methods below are for sub-classes tuned for particular database
    // servers which may or may not support all of JDBC. Please see 
    // MySQLExecutor for an example.
    protected boolean canGetGeneratedKeys(String SQL) {
        return SQL.trim().substring(0, "insert".length()).toLowerCase().equals("insert");
    }

    protected ResultSet getGeneratedKeys(PreparedStatement statement) throws SQLException {
        return statement.getGeneratedKeys();
    }

    protected SQLWarning getWarnings(ResultSet resultSet) throws SQLException {
        return resultSet.getWarnings();
    }

    protected ResultSet getResultSet(PreparedStatement statement) throws SQLException {
        return statement.getResultSet();
    }

    protected int getUpdateCount(PreparedStatement statement) throws SQLException {
        return statement.getUpdateCount();
    }

    protected SQLWarning getWarnings(Statement statement) throws SQLException {
        return statement.getWarnings();
    }

    protected int[] executeBatch(Statement statement) throws SQLException {
        return statement.executeBatch();
    }

    protected void addBatch(Statement statement, String row) throws SQLException {
        statement.addBatch(row);
    }

    protected void close(PreparedStatement statement) throws SQLException {
        statement.close();
    }

    protected void close(Statement statement) throws SQLException {
        statement.close();
    }

    protected SQLWarning getWarnings(PreparedStatement statement) throws SQLException {
        return statement.getWarnings();
    }

    protected int[] executeBatch(PreparedStatement statement) throws SQLException {
        return statement.executeBatch();
    }

    protected void addBatch(PreparedStatement statement) throws SQLException {
        statement.addBatch();
    }

    protected void close(ResultSet resultSet) throws SQLException {
        resultSet.close();
    }

    protected void setQueryTimeout(PreparedStatement statement, int queryTimeoutInSeconds) throws SQLException {
        statement.setQueryTimeout(queryTimeoutInSeconds);
    }

    protected void setMaxRowsToFetch(PreparedStatement statement, int maxRowsToFetch) throws SQLException {
        statement.setMaxRows(maxRowsToFetch);
    }

    protected void execute(PreparedStatement statement) throws SQLException {
        statement.execute();
    }

    protected PreparedStatement prepareGeneralStatement(String SQL) throws SQLException {
        return connection.prepareStatement(SQL);
    }

    protected PreparedStatement prepareInsertStatement(String SQL) throws SQLException {
        return connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
    }

    protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
        return connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
    }

    private PreparedStatement prepareExecuteStatement(String SQL) throws SQLException {
        if (canGetGeneratedKeys(SQL)) {
            return prepareInsertStatement(SQL);
        }
        else {
            return prepareGeneralStatement(SQL);
        }
    }

    protected void executeUpdate(Statement statement, String SQL) throws SQLException {
        statement.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
    }

    protected ResultSet getGeneratedKeys(Statement statement) throws SQLException {
        return statement.getGeneratedKeys();
    }

    protected void setParameter(PreparedStatement statement, Object object, int i) throws SQLException {
        if (object == null) {
            statement.setNull(i, java.sql.Types.VARCHAR);
        }
        else if (object instanceof NullValue.Binary) {
            statement.setNull(i, java.sql.Types.BINARY);
        }
        else if (object instanceof NullValue.Decimal) {
            statement.setNull(i, java.sql.Types.DECIMAL);
        }
        else if (object instanceof NullValue.Double) {
            statement.setNull(i, java.sql.Types.DOUBLE);
        }
        else if (object instanceof NullValue.Integer) {
            statement.setNull(i, java.sql.Types.INTEGER);
        }
        else if (object instanceof NullValue.String) {
            statement.setNull(i, java.sql.Types.VARCHAR);
        }
        else if (object instanceof NullValue.Timestamp) {
            statement.setNull(i, java.sql.Types.TIMESTAMP);
        }
        else if (object instanceof String) {
            statement.setString(i, (String) object);
        }
        else if (object instanceof Byte) {
            statement.setByte(i, (Byte) object);
        }
        else if (object instanceof Short) {
            statement.setShort(i, (Short) object);
        }
        else if (object instanceof Integer) {
            statement.setInt(i, (Integer) object);
        }
        else if (object instanceof Long) {
            statement.setLong(i, (Long) object);
        }
        else if (object instanceof BigDecimal) {
            statement.setBigDecimal(i, (BigDecimal) object);
        }
        else if (object instanceof Date) {
            statement.setDate(i, (Date) object);
        }
        else if (object instanceof Double) {
            statement.setDouble(i, (Double) object);
        }
        else if (object instanceof Float) {
            statement.setFloat(i, (Float) object);
        }
        else if (object instanceof Boolean) {
            statement.setBoolean(i, (Boolean) object);
        }
        else if (object instanceof Timestamp) {
            statement.setTimestamp(i, (Timestamp) object);
        }
        else if (object instanceof java.util.Date) {
            statement.setTimestamp(i, new Timestamp(((java.util.Date) object).getTime()));
        }
        else {
            statement.setObject(i, object);
        }
    }
}
