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

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author mabe02
 */
class DefaultDatabaseTransaction implements DatabaseTransaction, PooledConnectionUser
{
    private final TransactionIsolation transactionIsolation;
    private PooledDatabaseConnection pooledConnection;
    private SQLExecutor executor;
    private boolean initialized;

    DefaultDatabaseTransaction(PooledDatabaseConnection pooledConnection,
            TransactionIsolation transactionIsolation)
    {
        this.pooledConnection = pooledConnection;
        this.transactionIsolation = transactionIsolation;
        this.executor = pooledConnection.createExecutor();
        this.initialized = false;
    }

    @Override
    public synchronized void commit() throws SQLException
    {
        if(initialized)        
            pooledConnection.getConnection().commit();
        
        pooledConnection.done();
        pooledConnection = null;
        executor = null;
    }

    @Override
    public synchronized void rollback() throws SQLException
    {
        executor = null;
        if(pooledConnection != null) {
            try {
                if(initialized)
                    pooledConnection.getConnection().rollback();
            }
            finally {
                pooledConnection.done();    //Always mark as done!
                pooledConnection = null;
            }
        }
    }

    public void reset() 
    {
        try {
            rollback();
        }
        catch(SQLException ex) {
        }
    }

    @Override
    public void execute(ExecuteResultHandler handler, String SQL, Object... parameters) throws SQLException
    {
        if(pooledConnection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.execute(handler, SQL, parameters);
    }   

    
    @Override
    public synchronized void batchWrite(BatchUpdateHandler handler, String SQL, List<Object[]> parameters) throws SQLException
    {
        if(pooledConnection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.batchWrite(handler, SQL, parameters);
    }

    @Override
    public void batchWrite(BatchUpdateHandler handler, List<String> batchedSQL) throws SQLException
    {
        if(pooledConnection == null)
            throw new SQLException("Tried to call DefaultDatabaseTransaction.query after commit, rollback or revoked!");

        if(!initialized)
            initialize();

        executor.batchWrite(handler, batchedSQL);
    }

    private void initialize() throws SQLException
    {
        pooledConnection.setAutoCommit(false);
        pooledConnection.setTransactionIsolation(transactionIsolation.getConstant());
        /*
        Statement statement = pooledConnection.getConnection().createStatement();
        statement.execute("BEGIN TRAN");
        statement.close();
        */
        initialized = true;
    }
}
