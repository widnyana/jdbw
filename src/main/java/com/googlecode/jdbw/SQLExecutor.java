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

import java.sql.SQLException;
import java.util.List;

/**
 * The SQLExecutor is the object sending the query to the remote database server
 * and using a callback interface to process the result. This query will take care
 * of creating Statements and ResultSets and make sure to close everything when done.
 * This object is most frequently used through the sub-interface DatabaseTransaction
 * or AutoExecutor.
 * @see DatabaseTransaction
 * @see AutoExecutor
 * @author mabe02
 */
public interface SQLExecutor {

    /**
     * Sends a query to the database and handles any results through a callback interface
     * @param handler Callback interface to use for any results of the query
     * @param SQL SQL code to send to the database server, use ? for parameter substitution
     * @param parameters List of parameters to insert into the query, must be one for every ? used
     * @throws SQLException 
     */
    void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException;

    /**
     * Executes a list of queries as one batch on the remote database server
     * @param handler Callback interface to use for any results of the query
     * @param batchedSQL List of SQL to send to the remote server
     * @throws SQLException 
     */
    void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException;

    /**
     * Executes a batch query where the SQL is structurally the same but parameters 
     * are different. Usefull for inserting, updating or deleting multiple rows
     * from the same table.
     * @param handler Callback interface to use for any results of the query
     * @param SQL SQL to use for all queries, use ? for the parameter substitution
     * @param parameters List of object arrays, where one array equals one query sent to the server
     * @throws SQLException 
     */
    void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException;
}
