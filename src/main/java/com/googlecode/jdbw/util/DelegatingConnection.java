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

package com.googlecode.jdbw.util;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * This class is stolen from the Apache connection pool project,
 * I take no credit for it
 */
public abstract class DelegatingConnection implements Connection 
{
    protected Connection _conn = null;
    protected boolean _closed = false;
    private boolean _cacheState = true;
    private Boolean _autoCommitCached = null;
    private Boolean _readOnlyCached = null;

    /**
     * Create a wrapper for the Connection which traces this
     * Connection in the AbandonedObjectPool.
     *
     * @param c the {@link Connection} to delegate all calls to.
     */
    public DelegatingConnection(Connection c) {
        super();
        _conn = c;
    }

    /**
     * Returns a string representation of the metadata associated with
     * the innnermost delegate connection.
     * 
     * @since 1.2.2
     */
    @Override
    public String toString() {
        String s = null;

        Connection c = this.getInnermostDelegateInternal();
        if(c != null) {
            try {
                if(c.isClosed()) {
                    s = "connection is closed";
                }
                else {
                    DatabaseMetaData meta = c.getMetaData();
                    if(meta != null) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(meta.getURL());
                        sb.append(", UserName=");
                        sb.append(meta.getUserName());
                        sb.append(", ");
                        sb.append(meta.getDriverName());
                        s = sb.toString();
                    }
                }
            }
            catch(SQLException ex) {
                // Ignore
            }
        }

        if(s == null) {
            s = super.toString();
        }

        return s;
    }

    /**
     * Returns my underlying {@link Connection}.
     * @return my underlying {@link Connection}.
     */
    public Connection getDelegate() {
        return getDelegateInternal();
    }

    /**
     * Should be final but can't be for compatibility with previous releases.
     */
    protected Connection getDelegateInternal() {
        return _conn;
    }

    /**
     * Compares innermost delegate to the given connection.
     * 
     * @param c connection to compare innermost delegate with
     * @return true if innermost delegate equals <code>c</code>
     * @since 1.2.2
     */
    public boolean innermostDelegateEquals(Connection c) {
        Connection innerCon = getInnermostDelegateInternal();
        if(innerCon == null) {
            return c == null;
        }
        else {
            return innerCon.equals(c);
        }
    }

    /**
     * This method considers two objects to be equal 
     * if the underlying jdbc objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        Connection delegate = getInnermostDelegateInternal();
        if(obj instanceof DelegatingConnection) {
            DelegatingConnection c = (DelegatingConnection) obj;
            Connection cDelegate = c.getInnermostDelegateInternal();
            return delegate == cDelegate || (delegate != null && delegate.equals(cDelegate));
        }
        else {
            return delegate.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        Object obj = getInnermostDelegateInternal();
        if(obj == null) {
            return 0;
        }
        return obj.hashCode();
    }

    /**
     * If my underlying {@link Connection} is not a
     * <tt>DelegatingConnection</tt>, returns it,
     * otherwise recursively invokes this method on
     * my delegate.
     * <p>
     * Hence this method will return the first
     * delegate that is not a <tt>DelegatingConnection</tt>,
     * or <tt>null</tt> when no non-<tt>DelegatingConnection</tt>
     * delegate can be found by traversing this chain.
     * <p>
     * This method is useful when you may have nested
     * <tt>DelegatingConnection</tt>s, and you want to make
     * sure to obtain a "genuine" {@link Connection}.
     */
    public Connection getInnermostDelegate() {
        return getInnermostDelegateInternal();
    }

    protected final Connection getInnermostDelegateInternal() {
        Connection c = _conn;
        while(c != null && c instanceof DelegatingConnection) {
            c = ((DelegatingConnection) c).getDelegateInternal();
            if(this == c) {
                return null;
            }
        }
        return c;
    }

    /** Sets my delegate. */
    public void setDelegate(Connection c) {
        _conn = c;
    }

    protected void handleException(SQLException e) throws SQLException {
        throw e;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return _conn.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency) throws SQLException {
        return _conn.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return _conn.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return _conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return _conn.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql,
                                         int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        return prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public void clearWarnings() throws SQLException {
        _conn.clearWarnings();
    }

    @Override
    public void commit() throws SQLException {
        _conn.commit();
    }

    /**
     * Returns the state caching flag.
     * 
     * @return  the state caching flag
     */
    public boolean getCacheState() {
        return _cacheState;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        if(_cacheState && _autoCommitCached != null) {
            return _autoCommitCached.booleanValue();
        }
        _autoCommitCached = Boolean.valueOf(_conn.getAutoCommit());
        return _autoCommitCached.booleanValue();
    }

    @Override
    public String getCatalog() throws SQLException {
        return _conn.getCatalog();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return _conn.getMetaData();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return _conn.getTransactionIsolation();
    }

    @Override
    public Map getTypeMap() throws SQLException {
        return _conn.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return _conn.getWarnings();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        if(_cacheState && _readOnlyCached != null) {
            return _readOnlyCached.booleanValue();
        }
        _readOnlyCached = Boolean.valueOf(_conn.isReadOnly());
        return _readOnlyCached.booleanValue();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return _conn.nativeSQL(sql);
    }

    @Override
    public void rollback() throws SQLException {
        _conn.rollback();
    }

    /**
     * Sets the state caching flag.
     * 
     * @param cacheState    The new value for the state caching flag
     */
    public void setCacheState(boolean cacheState) {
        this._cacheState = cacheState;
    }

    /**
     * Can be used to clear cached state when it is known that the underlying
     * connection may have been accessed directly.
     */
    public void clearCachedState() {
        _autoCommitCached = null;
        _readOnlyCached = null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        try {
            _conn.setAutoCommit(autoCommit);
            if(_cacheState)
                _autoCommitCached = Boolean.valueOf(autoCommit);
        }
        catch(SQLException e) {
            _autoCommitCached = null;
            throw e;
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        _conn.setCatalog(catalog);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        try {
            _conn.setReadOnly(readOnly);
            if(_cacheState)
                _readOnlyCached = Boolean.valueOf(readOnly);
        }
        catch(SQLException e) {
            _readOnlyCached = null;
            throw e;
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        _conn.setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(Map map) throws SQLException {
        _conn.setTypeMap(map);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return _closed || _conn.isClosed();
    }

    protected void checkOpen() throws SQLException {
        if(_closed) {
            if(null != _conn) {
                String label = "";
                try {
                    label = _conn.toString();
                }
                catch(Exception ex) {
                    // ignore, leave label empty
                }
                throw new SQLException("Connection " + label + " is closed.");
            }
            else {
                throw new SQLException("Connection is null.");
            }
        }
    }

    @Override
    public int getHoldability() throws SQLException {
        return _conn.getHoldability();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        _conn.setHoldability(holdability);
    }

    @Override
    public java.sql.Savepoint setSavepoint() throws SQLException {
        return _conn.setSavepoint();
    }

    @Override
    public java.sql.Savepoint setSavepoint(String name) throws SQLException {
        return _conn.setSavepoint(name);
    }

    @Override
    public void rollback(java.sql.Savepoint savepoint) throws SQLException {
        _conn.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
        _conn.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        return _conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return _conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        return _conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return _conn.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
        return _conn.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
        return _conn.prepareStatement(sql, columnNames);
    }

    /* JDBC_4_ANT_KEY_BEGIN */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass()) || _conn.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if(iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        else if(iface.isAssignableFrom(_conn.getClass())) {
            return iface.cast(_conn);
        }
        else {
            return _conn.unwrap(iface);
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return _conn.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return _conn.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return _conn.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return _conn.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return _conn.createSQLXML();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return _conn.createStruct(typeName, attributes);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return _conn.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        _conn.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        _conn.setClientInfo(properties);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return _conn.getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return _conn.getClientInfo(name);
    }
    
    /*
     * Java 7 methods, what should we do about these?
    @Override
    public void setSchema(String schema) throws SQLException {
        _conn.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return _conn.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        _conn.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        _conn.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return _conn.getNetworkTimeout();
    }
    */
}
