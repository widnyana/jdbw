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
package com.googlecode.jdbw.server;

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.impl.DatabaseConnectionImpl;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;

/**
 * A common base class for many types of {@code DatabaseServer}s. This class
 * provides some helper methods and some default implementations for the
 * interface methods. 
 * 
 * <p>If you want to create a custom {@code DatabaseServer} implementation,
 * you probably want to extend {@code StandardDatabaseServer} instead of this
 * class, as it has helper methods for network parameters and authentication.
 * @see StandardDatabaseServer
 * @author mabe02
 */
public abstract class AbstractDatabaseServer implements DatabaseServer {
    
    private final JDBCDriverDescriptor driverDescriptor;

    public AbstractDatabaseServer(JDBCDriverDescriptor driverDescriptor) {
        this.driverDescriptor = driverDescriptor;
    }
    
    public Properties getConnectionProperties() {
        return new Properties();
    }
    
    public DatabaseConnection connect(final DataSourceCreator dataSourceFactory) {
        registerJDBCDriver(driverDescriptor.getDriverClassName());
        return new DatabaseConnectionImpl(
                dataSourceFactory.newDataSource(getJDBCUrl(), getConnectionProperties()),
                new DataSourceCloser() {
                    public void closeDataSource(DataSource dataSource) {
                        dataSourceFactory.close(dataSource);
                    }
                },
                getServerType());
    }
    
    protected abstract String getJDBCUrl();
    
    protected JDBCDriverDescriptor getDriverDescriptor() {
        return driverDescriptor;
    }
    
    private static final Set<String> REGISTERED_DRIVERS = new HashSet<String>();
    private void registerJDBCDriver(String driverClassName) {
        synchronized(REGISTERED_DRIVERS) {
            if(REGISTERED_DRIVERS.contains(driverClassName))
                return;
            
            try {
                Class.forName(driverClassName).newInstance();
                REGISTERED_DRIVERS.add(driverClassName);
            }
            catch(Exception e) {
                throw new IllegalStateException("Unable to load the JDBC driver \"" + driverClassName + "\"");
            }
        }
    }
}
