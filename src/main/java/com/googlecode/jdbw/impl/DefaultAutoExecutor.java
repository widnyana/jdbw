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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
class DefaultAutoExecutor implements SQLExecutor, PooledConnectionUser
{
    private final DefaultDatabaseConnection sourcePool;

    DefaultAutoExecutor(DefaultDatabaseConnection sourcePool)
    {
        this.sourcePool = sourcePool;
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException
    {
        PooledDatabaseConnection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = connection.createExecutor();
                executor.execute(handler, SQL, parameters);
                connection.done();
                return;
            }
            catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    connection.bad();
                    sleep(500);
                    continue;
                }
                else {
                    connection.done(); //Syntax error?
                    throw e;
                }
            }
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException
    {
        PooledDatabaseConnection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = connection.createExecutor();
                executor.batchWrite(handler, SQL, parameters);
                connection.done();
                return;
            }
            catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    connection.bad();
                    sleep(500);
                    continue;
                }
                else {
                    connection.done(); //Syntax error?
                    throw e;
                }
            }
        }
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException
    {
        PooledDatabaseConnection connection = null;
        while(true) {
            try {
                connection = getNewConnection();
                SQLExecutor executor = connection.createExecutor();
                executor.batchWrite(handler, batchedSQL);
                connection.done();
                return;
            }
            catch(SQLException e) {
                if(sourcePool.isConnectionError(e)) {
                    connection.bad();
                    sleep(500);
                    continue;
                }
                else {
                    connection.done(); //Syntax error?
                    throw e;
                }
            }
        }
    }

    private PooledDatabaseConnection getNewConnection() throws SQLException
    {
        PooledDatabaseConnection connection;
        connection = sourcePool.getPooledConnection();
        connection.connectionUser(this);
        connection.setAutoCommit(true);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        return connection;
    }

    public void reset() 
    {
        //No action needed
    }

    private void sleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {}
    }
}
