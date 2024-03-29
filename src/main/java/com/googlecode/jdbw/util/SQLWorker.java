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
package com.googlecode.jdbw.util;

import com.googlecode.jdbw.ResultSetInformation;
import com.googlecode.jdbw.SQLExecutor;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This utility class can be very helpful when sending simple queries to the database and you don't want to get too
 * involved in the details. It will help you to send queries and get result back in simple and familiar formats, as well
 * as grabbing only limited parts of the result set.
 * <p/>
 * You create an SQLWorker on top of an SQLExecutor, so it supports both using AutoExecutor and using transactions.
 *
 * @author Martin Berglund
 */
public class SQLWorker {

    private final SQLExecutor executor;

    /**
     * Creates a new SQLWorker with a specified underlying SQLExecutor to use for the actual database communication.
     *
     * @param executor SQLExecutor send the queries to
     */
    public SQLWorker(SQLExecutor executor) {
        this.executor = executor;
    }

    /**
     * Sends a query to the database and returns the whole ResultSet as a list of Object arrays.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The entire result set, converted into a list of Object arrays, where each array in the list is one row in
     * the result set
     * @throws SQLException If any database error occurred
     */
    public List<Object[]> query(String SQL, Object... parameters) throws SQLException {
        final List<Object[]> result = new ArrayList<Object[]>();
        executor.execute(new ExecuteResultHandlerAdapter() {
            @Override
            public boolean nextRow(Object[] row) {
                result.add(row);
                return true;
            }
        }, SQL, parameters);
        return result;
    }

    /**
     * Sends a query to the database and returns the whole ResultSet as a list of String arrays.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The entire result set, converted into a list of String arrays, where each array in the list is one row in
     * the result set and each element in the String arrays are the .toString() call on the underlying result set
     * object.
     * @throws SQLException If any database error occurred
     */
    public List<String[]> queryAsStrings(String SQL, Object... parameters) throws SQLException {
        List<String[]> result = new ArrayList<String[]>();
        for (Object[] row : query(SQL, parameters)) {
            String[] stringRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    stringRow[i] = null;
                } else {
                    stringRow[i] = row[i].toString();
                }
            }
            result.add(stringRow);
        }
        return result;
    }

    /**
     * Sends a query to the database server and expects nothing to return.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @throws SQLException If any database error occurred
     */
    public void write(String SQL, Object... parameters) throws SQLException {
        executor.execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
    }

    /**
     * Sends a query to the database server and returns any auto-generated value the database is telling us about.
     * Please note that not all database servers supports this feature.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return An auto-generated value created by the database as a result of this query, or null
     * @throws SQLException If any database error occurred
     */
    public Object insert(String SQL, Object... parameters) throws SQLException {
        final List<Object> autoGeneratedKeys = new ArrayList<Object>();
        executor.execute(new ExecuteResultHandlerAdapter() {
            @Override
            public void onGeneratedKey(Object object) {
                autoGeneratedKeys.add(object);
            }
        }, SQL, parameters);
        if (autoGeneratedKeys.isEmpty()) {
            return null;
        } else {
            return autoGeneratedKeys.get(0);
        }
    }

    /**
     * Sends a query to the database and returns the first row of the result set as an Object array.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first row of the result set, converted into an Object array
     * @throws SQLException If any database error occurred
     */
    public Object[] top(String SQL, Object... parameters) throws SQLException {
        final List<Object[]> result = new ArrayList<Object[]>();
        executor.execute(new ExecuteResultHandlerAdapter() {
            @Override
            public boolean nextRow(Object[] row) {
                result.add(row);
                return false;
            }
        }, 1, 0, SQL, parameters);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Sends a query to the database and returns the first row of the result set as a String array.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first row of the result set, converted into an String array where each element in the array is the
     * .toString() result of each object in the result set
     * @throws SQLException If any database error occurred
     */
    public String[] topAsString(String SQL, Object... parameters) throws SQLException {
        final Object[] rowAsObjects = top(SQL, parameters);
        final String[] rowAsStrings = new String[rowAsObjects.length];
        for (int i = 0; i < rowAsObjects.length; i++) {
            rowAsStrings[i] = rowAsObjects[i] != null ? rowAsObjects[i].toString() : null;
        }
        return rowAsStrings;
    }

    /**
     * Sends a query to the database and returns the first column of every row
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of every row as a list, where the order is maintained from the result set
     * @throws SQLException If any database error occurred
     */
    public List<Object> leftColumn(String SQL, Object... parameters) throws SQLException {
        List<Object[]> allRows = query(SQL, parameters);
        List<Object> result = new ArrayList<Object>();
        for (Object[] row : allRows) {
            result.add(row[0]);
        }
        return result;
    }

    /**
     * Sends a query to the database and returns the first column of every row as a list of Strings
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of every row as a list of Strings, where the order of the list is maintained from the
     * result set and each element in the list is the result of calling .toString() on the object in the result set.
     * @throws SQLException If any database error occurred
     */
    public List<String> leftColumnAsString(String SQL, Object... parameters) throws SQLException {
        List<Object> leftColumn = leftColumn(SQL, parameters);
        List<String> result = new ArrayList<String>(leftColumn.size());
        for (Object value : leftColumn) {
            if (value == null) {
                result.add(null);
            } else {
                result.add(value.toString());
            }
        }
        return result;
    }

    /**
     * Sends a query to the database and returns the first column of the first row
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of the first row in the result set
     * @throws SQLException If any database error occurred
     */
    public Object topLeftValue(String SQL, Object... parameters) throws SQLException {
        Object[] row = top(SQL, parameters);
        if (row == null) {
            return null;
        } else {
            return row[0];
        }
    }

    /**
     * Sends a query to the database and returns the first column of the first row, as a String
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of the first row in the result set, as a String
     * @throws SQLException If any database error occurred
     */
    public String topLeftValueAsString(String SQL, Object... parameters) throws SQLException {
        final Object value = topLeftValue(SQL, parameters);
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }

    /**
     * Sends a query to the database and returns the first column of the first row, as an Integer
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of the first row in the result set, as an Integer
     * @throws SQLException If any database error occurred
     */
    public Integer topLeftValueAsInt(String SQL, Object... parameters) throws SQLException {
        Object value = topLeftValue(SQL, parameters);
        if (value == null) {
            return null;
        } else {
            return Integer.parseInt(value.toString());
        }
    }

    /**
     * Sends a query to the database and returns the first column of the first row, as a Long
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of the first row in the result set, as a Long
     * @throws SQLException If any database error occurred
     */
    public Long topLeftValueAsLong(String SQL, Object... parameters) throws SQLException {
        Object value = topLeftValue(SQL, parameters);
        if (value == null) {
            return null;
        } else {
            return Long.parseLong(value.toString());
        }
    }

    /**
     * Sends a query to the database and returns the first column of the first row, as a BigInteger
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The first column of the first row in the result set, as a BigInteger
     * @throws SQLException If any database error occurred
     */
    public BigInteger topLeftValueAsBigInteger(String SQL, Object... parameters) throws SQLException {
        Object value = topLeftValue(SQL, parameters);
        if (value == null) {
            return null;
        } else {
            return new BigInteger(value.toString());
        }
    }

    /**
     * Sends a query to the database and returns the whole result as a {@code DataSet} of only Strings.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The whole result set converted into Strings and read into a {@code DataSet}
     * @throws SQLException If any database error occurred
     */
    public DataSet<String> dataSetOfStrings(String SQL, Object... parameters) throws SQLException {
        return dataSet(String.class, new ObjectMapper<Object, String>() {
            @Override
            public String invoke(Object param) {
                return param.toString();
            }
        }, SQL, parameters);
    }

    /**
     * Sends a query to the database and returns the whole result as a {@code DataSet} of only Longs.
     *
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The whole result set converted into Longs and read into a {@code DataSet}
     * @throws SQLException If any database error occurred
     */
    public DataSet<Long> dataSetOfLong(String SQL, Object... parameters) throws SQLException {
        return dataSet(Long.class, new ObjectMapper<Object, Long>() {
            @Override
            public Long invoke(Object param) {
                try {
                    return Long.parseLong(param.toString());
                }
                catch(NumberFormatException e) {
                    return null;
                }
            }
        }, SQL, parameters);
    }

    /**
     * Sends a query to the database and returns the whole result as a {@code DataSet} of a specified type.
     *
     * @param typeClass What type build the {@code DataSet} of
     * @param mapper Converter to use when converting the result set values into the target type
     * @param SQL SQL to send to the database server
     * @param parameters Parameters to substitute ?:s for in the SQL string
     * @return The whole result set converted into the specified format and read into a {@code DataSet}
     * @throws SQLException If any database error occurred
     */
    public <V> DataSet<V> dataSet(Class<V> typeClass, ObjectMapper<Object, V> mapper, String SQL, Object... parameters) throws SQLException {
        ResultSetConverter<V> converter = new ResultSetConverter(typeClass, mapper);
        executor.execute(converter, SQL, parameters);
        return converter.builder.build();
    }

    /**
     * Interface for declaring how to convert from one type to another
     * @param <S> Type to go from
     * @param <T> Type to go to
     */
    public static interface ObjectMapper<S, T> {
        T invoke(S param);
    }

    private static class ResultSetConverter<V> extends ExecuteResultHandlerAdapter {
        private final Class<V> typeClass;
        private final ObjectMapper<Object, V> converter;
        DataSet.Builder<V> builder;

        public ResultSetConverter(Class<V> typeClass, ObjectMapper<Object, V> converter) {
            this.typeClass = typeClass;
            this.converter = converter;
        }

        @Override
        public boolean onResultSet(ResultSetInformation information) {
            builder = new DataSet.Builder<V>(information.getColumnLabels());
            return information.getResultSetIndex() == 0;
        }

        @Override
        public boolean nextRow(Object[] row) {
            V[] newRow = (V[])Array.newInstance(typeClass, row.length);
            for(int i = 0; i < row.length; i++) {
                if(row[i] == null) {
                    newRow[i] = null;
                }
                else {
                    newRow[i] = converter.invoke(row[i]);
                }
            }
            builder.addRow(newRow);
            return true;
        }
    }
}
