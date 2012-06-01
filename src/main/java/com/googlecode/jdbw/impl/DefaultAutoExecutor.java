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
 * Copyright (C) 2009-2012 mabe02
 */
package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.BatchUpdateHandler;
import com.googlecode.jdbw.ExecuteResultHandler;
import com.googlecode.jdbw.SQLExecutor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
class DefaultAutoExecutor implements SQLExecutor {

    private final DefaultDatabaseConnection sourcePool;

    DefaultAutoExecutor(DefaultDatabaseConnection sourcePool) {
        this.sourcePool = sourcePool;
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = new DefaultSQLExecutor(connection);
                executor.execute(handler, SQL, parameters);
                return;
            } catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    sleep(500);
                    continue;
                } else {
                    throw e;  //Syntax error?
                }
            } finally {
                connection.close();
            }
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = new DefaultSQLExecutor(connection);
                executor.batchWrite(handler, SQL, parameters);
                return;
            } catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    sleep(500);
                    continue;
                } else {
                    throw e;  //Syntax error?
                }
            } finally {
                connection.close();
            }
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException {
        Connection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = new DefaultSQLExecutor(connection);
                executor.batchWrite(handler, batchedSQL);
                return;
            } catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    sleep(500);
                    continue;
                } else {
                    throw e;
                }
            } finally {
                connection.close();
            }
        }
    }

    private Connection getNewConnection() throws SQLException {
        Connection connection = sourcePool.getConnection();
        connection.setAutoCommit(true);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        return connection;
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
        }
    }
}
