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
package com.googlecode.jdbw.server;

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A common base class for many types of {@code DatabaseServer}s. This class
 * provides some helper methods and some default implementations for the
 * interface methods. 
 * 
 * <p>If you want to create a custom {@code DatabaseServer} implementation,
 * you probably want to extend {@code StandardDatabaseServer} instead of this
 * class, as it has helper methods for network parameters and authentication.
 * @see StandardDatabaseServer
 * @author Martin Berglund
 */
public abstract class AbstractDatabaseServer<T extends DatabaseConnectionFactory> implements DatabaseServer<T> {
    
    private final JDBCDriverDescriptor<T> driverDescriptor;

    public AbstractDatabaseServer(JDBCDriverDescriptor<T> driverDescriptor) {
        this.driverDescriptor = driverDescriptor;
        registerJDBCDriver(driverDescriptor.getDriverClassName());
    }

    @Override
    public void testConnection(String username, String password) throws SQLException {
        DatabaseConnectionFactory connectionFactory = newConnectionFactory();
        if(connectionFactory instanceof AuthenticatingDatabaseConnectionFactory) {
            ((AuthenticatingDatabaseConnectionFactory)connectionFactory).setUsername(username);
            ((AuthenticatingDatabaseConnectionFactory)connectionFactory).setPassword(password);            
        }
        connectionFactory.connect(new OneSharedConnectionDataSource.Factory()).close();
    }
    
    protected JDBCDriverDescriptor<T> getDriverDescriptor() {
        return driverDescriptor;
    }

    @Override
    public T newConnectionFactory() {
        return getDriverDescriptor().createDatabaseConnectionFactory(this);
    }
    
    private static final Set<String> REGISTERED_DRIVERS = new ConcurrentSkipListSet<String>();
    private void registerJDBCDriver(String driverClassName) {
        if(REGISTERED_DRIVERS.add(driverClassName)) {
            try {
                Class.forName(driverClassName).newInstance();
            }
            catch(Exception e) {
                REGISTERED_DRIVERS.remove(driverClassName);
                throw new IllegalStateException("Unable to load the JDBC driver \"" + driverClassName + "\": " + e.getMessage());
            }            
        }
    }
}
