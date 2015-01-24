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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Package private class used by {@code DatabaseConnectionImpl} as the transaction implementation.
 *
 * @author Martin Berglund
 */
class DatabaseTransactionImpl implements DatabaseTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTransactionImpl.class);
    private final TransactionIsolation transactionIsolation;
    private Connection connection;
    private SQLExecutor executor;
    private boolean initialized;

    DatabaseTransactionImpl(
            Connection connection,
            SQLExecutor executor,
            TransactionIsolation transactionIsolation) {
        this.connection = connection;
        this.transactionIsolation = transactionIsolation;
        this.executor = executor;
        this.initialized = false;
    }

    @Override
    public synchronized void commit() throws SQLException {
        executor = null;
        try {
            if (initialized) {
                connection.commit();
            }
        }
        finally {
            try {
                connection.close();
            }
            catch (SQLException e) {
                LOGGER.error("Cannot close/return the database connection after commit()", e);
            }
        }
        connection = null;
    }

    @Override
    public synchronized void rollback() throws SQLException {
        executor = null;
        try {
            if (initialized) {
                connection.rollback();
            }
        }
        finally {
            try {
                connection.close();
            }
            catch (SQLException e) {
                LOGGER.error("Cannot close/return the database connection after rollback()", e);
            }
        }
        connection = null;
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
    public void execute(ExecuteResultHandler handler, int maxRowsToFetch, int queryTimeout, String SQL, Object... parameters) throws SQLException {
        if (connection == null) {
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");
        }

        if (!initialized) {
            initialize();
        }

        executor.execute(handler, maxRowsToFetch, queryTimeout, SQL, parameters);
    }

    @Override
    public void batchWrite(String SQL, List<Object[]> parameters) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), SQL, parameters);
    }

    @Override
    public synchronized void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException {
        if (connection == null) {
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");
        }

        if (!initialized) {
            initialize();
        }

        executor.batchWrite(handler, SQL, parameters);
    }

    @Override
    public void batchWrite(List<String> batchedSQL) throws SQLException {
        batchWrite(new BatchUpdateHandlerAdapter(), batchedSQL);
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException {
        if (connection == null) {
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");
        }

        if (!initialized) {
            initialize();
        }

        executor.batchWrite(handler, batchedSQL);
    }

    private void initialize() throws SQLException {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(transactionIsolation.getConstant());
        initialized = true;
    }
}
