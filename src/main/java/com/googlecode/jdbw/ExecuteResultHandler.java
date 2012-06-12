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
package com.googlecode.jdbw;

import java.sql.SQLWarning;
import java.util.List;

/**
 *
 * @author mabe02
 */
public interface ExecuteResultHandler {

    /**
     * This method is called just before the query is sent to the database, 
     * setting the property on the Statement which limits the number of rows
     * we want back. It's up to the driver implementation and the server if this
     * limit will be honored or not (I think)
     * @return Number of rows to fetch from the server, 0 means no limit
     */
    int getMaxRowsToFetch();

    /**
     * This callback is invoked when a new result set is read from the server,
     * if the result set contains any rows they will appear in subsequent calls
     * to the nextRow(Object[] row) callback.
     * @param columnNames List of all column names
     * @param columnTypes List of all column types (see java.sql.Types)
     */
    void onResultSet(List<String> columnNames, List<Integer> columnTypes);

    /**
     * The callback is called once for every row returned by a result set. The
     * row belongs to the result set defined by the last call to onResultSet.
     * @param row All the values of the row
     * @return true if you want to read more rows, false if you want to close 
     * the result set and skip remaining rows
     */
    boolean nextRow(Object[] row);
    
    /**
     * This method is called in a multi result set query after one result set
     * have been read and another one is coming. Expect another call to 
     * onResultSet as well, for the next result set.
     * @return true if you want to read the next result set, false if you want
     * to skip remaining result sets and close the statement
     */
    boolean nextResultSet();

    /**
     * Callback called for queries that updated rows
     * @param updateCount Number of rows affected by the query, as reported by
     * the server
     */
    void onUpdateCount(int updateCount);

    /**
     * When a key has been generated on the server by the query, this callback
     * is called with that value
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
