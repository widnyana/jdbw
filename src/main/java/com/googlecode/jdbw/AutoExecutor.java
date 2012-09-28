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

import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * An auto executor will automatically allocate a connection from the pool when its SQL methods are
 * called and return the connection and all resources once done. If a query fails, it will
 * investigate the error and retry if the error is judged to be retryable.
 *
 * @author Martin Berglund
 */
public class AutoExecutor implements SQLExecutor {

    private final DataSource dataSource;
    private final DatabaseServerType serverType;

    public AutoExecutor(DataSource dataSource, DatabaseServerType serverType) {
        this.dataSource = dataSource;
        this.serverType = serverType;
    }

    /**
     * Shortcut for calling execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
     */
    public void execute(String SQL, Object... parameters) throws SQLException {
        execute(new ExecuteResultHandlerAdapter(), SQL, parameters);
    }
    
    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.execute(handler, SQL, parameters);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(500);
                    continue;
                }
                else {
                    throw e;  //Syntax error?
                }
            }
            finally {
                if(connection != null) {
                    connection.close();
                }
            }
        }
    }

    /**
     * Shortcut for calling batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL, parameters);
     */
    public void batchWrite(String SQL, List<Object[]> parameters) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), SQL, parameters);
    }
    
    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.batchWrite(handler, SQL, parameters);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(500);
                    continue;
                }
                else {
                    throw e;  //Syntax error?
                }
            }
            finally {
                connection.close();
            }
        }
    }

    /**
     * Shortcut for calling batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
     */
    public void batchWrite(List<String> batchedSQL) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
    }
    
    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = createSQLExecutor(connection);
                executor.batchWrite(handler, batchedSQL);
                return;
            }
            catch(SQLException e) {
                if(serverType.isConnectionError(e)) {
                    sleep(500);
                    continue;
                }
                else {
                    throw e;
                }
            }
            finally {
                connection.close();
            }
        }
    }

    private SQLExecutor createSQLExecutor(Connection connection) {
        return serverType.createExecutor(connection);
    }

    private Connection getNewConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        return connection;
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {
        }
    }
}
