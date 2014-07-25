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
package com.googlecode.jdbw;

import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import java.sql.SQLWarning;
import java.util.List;

/**
 * This interface is used to handle results when executing custom SQL code. It 
 * is passed in to an SQLExecutor and when as the executor runs the SQL, it will
 * call the methods on this handler as results are coming in.
 * 
 * If you don't need all methods of this interface, you can extend the 
 * {@code ExecuteResultHandlerAdapter} instead.
 * @see ExecuteResultHandlerAdapter
 * @author Martin Berglund
 */
public interface ExecuteResultHandler {
    /**
     * This callback is invoked when a new result set is read from the server.
     * If the result set contains any rows they will appear in subsequent calls
     * to the {@code nextRow(Object[] row)} callback. The whole ResultSet has been read
     * through when either {@code onResultSet()} or {@code onDone()} are called.
     * <p/>
     * You can control if you want the result set to be processed by returning {@code true}
     * or {@code false}. If you return {@code false}, no rows will be read and the
     * ResultSet is immediately closed. Please note that some JDBC drivers
     * (MySQL, for example) will still stream down all rows from the server so
     * closing it could take a significant amount of time. If you return {@code true},
     * the result set is iterated and JDBW calls {@code nextRow(..)} on each row.
     * @return {@code true} if you want to read through the ResultSet, {@code false}
     * if you want to skip it
     * <p/>
     * If your query returns multiple result sets, they will be read in order and
     * {@code onResultSet(..)} is called on each one in turn.
     */
    boolean onResultSet(ResultSetInformation information);

    /**
     * The callback is called once for every row returned by a result set. The
     * row belongs to the result set defined by the last call to onResultSet.
     * @param row All the values of the row
     * @return true if you want to read more rows, false if you want to close 
     * the result set and skip remaining rows
     */
    boolean nextRow(Object[] row);

    /**
     * Callback called for queries that updated rows. Not all servers supports this
     * and some JDBC drivers doesn't handle it correctly.
     * @param updateCount Number of rows affected by the query, as reported by
     * the server
     */
    void onUpdateCount(int updateCount);

    /**
     * When a key has been generated on the server by the query, this callback
     * is called with that value. Not all servers/queries/JDBC drivers supports this.
     * @param object Generated key value
     */
    void onGeneratedKey(Object object);

    /**
     * Callback called for every SQLWarning the server sent for the query
     * @param warning SQLWarning from the server
     */
    void onWarning(SQLWarning warning);

    /**
     * This method is called when all processing of the query is done and the
     * statement is about to be closed.
     */
    void onDone();
}
