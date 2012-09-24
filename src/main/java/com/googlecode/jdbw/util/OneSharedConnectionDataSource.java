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

package com.googlecode.jdbw.util;

import com.googlecode.jdbw.DataSourceFactory;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Very primitive database connection source which only keeps one connection
 * in the pool. There is no reconnection logic or anything, it just hands out
 * the same connection to one user at a time. Calling getConnection() on this
 * object while another process is using that connection will block until the
 * connection is returned to the pool.
 * @author mabe02
 */
public class OneSharedConnectionDataSource implements DataSource {
    
    public static class Factory implements DataSourceFactory {
        public DataSource newDataSource(String jdbcUrl, Properties properties) {
            try {
                return new OneSharedConnectionDataSource(
                        DriverManager.getConnection(jdbcUrl, properties));
            }
            catch(SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        public void close(DataSource previouslyConstructedDataSource) {
            ((OneSharedConnectionDataSource)previouslyConstructedDataSource).close();
        }        
    }
    
    private final ArrayBlockingQueue<Connection> connectionQueue;

    public OneSharedConnectionDataSource(Connection connection) {
        this.connectionQueue = new ArrayBlockingQueue<Connection>(1);
        this.connectionQueue.add(connection);
    }

    public void close() {
        try {
            connectionQueue.poll().close();
        }
        catch(SQLException e) {
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return new DelegatingConnection(connectionQueue.take()) {
                public void close() throws SQLException {
                    //We don't need to call .offer(...) here since the capacity is only 1
                    connectionQueue.add(_conn);
                }
            };
        }
        catch(InterruptedException e) {
            return null;
        }
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
