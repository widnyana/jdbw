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

import com.googlecode.jdbw.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
class DatabaseTransactionImpl implements DatabaseTransaction
{
    private final TransactionIsolation transactionIsolation;
    private Connection connection;
    private SQLExecutor executor;
    private boolean initialized;

    DatabaseTransactionImpl(
            Connection connection,
            SQLExecutor executor,
            TransactionIsolation transactionIsolation)
    {
        this.connection = connection;
        this.transactionIsolation = transactionIsolation;
        this.executor = executor;
        this.initialized = false;
    }

    @Override
    public synchronized void commit() throws SQLException
    {
        executor = null;
        try {
            if(initialized)        
                connection.commit();
        }
        finally {
            try {
                connection.close();
            }
            catch(SQLException e) {                
            }
        }
        connection = null;
    }

    @Override
    public synchronized void rollback() throws SQLException
    {
        executor = null;
        try {
            if(initialized)        
                connection.rollback();
        }
        finally {
            try {
                connection.close();
            }
            catch(SQLException e) {                
            }
        }
        connection = null;
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException
    {
        if(connection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.execute(handler, SQL, parameters);
    }   

    
    @Override
    public synchronized void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException
    {
        if(connection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.batchWrite(handler, SQL, parameters);
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException
    {
        if(connection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.batchWrite(handler, batchedSQL);
    }

    private void initialize() throws SQLException
    {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(transactionIsolation.getConstant());
        initialized = true;
    }
}
