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
package com.googlecode.jdbw.server;

import com.googlecode.jdbw.DataSourceCloser;
import com.googlecode.jdbw.DataSourceCreator;
import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.impl.DatabaseConnectionImpl;
import java.util.Properties;
import javax.sql.DataSource;

/**
 *
 * @author mabe02
 */
public abstract class StandardDatabaseServer implements NetworkDatabaseServer, MultiCatalogDatabaseServer, UserAuthenticatedDatabaseServer {

    private final JDBCDriverDescriptor driverDescriptor;
    private final String hostname;
    private final int port;
    private final String catalog;
    private final String username;
    private final String password;

    protected StandardDatabaseServer(
            JDBCDriverDescriptor driverDescriptor,
            String hostname, 
            int port, 
            String catalog, 
            String username, 
            String password) {
        this.driverDescriptor = driverDescriptor;
        this.hostname = hostname;
        this.port = port;
        this.catalog = catalog;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getDefaultCatalog() {
        return catalog;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    private String getJDBCUrl() {
        return driverDescriptor.formatJDBCUrl(hostname, port, catalog);
    }

    public DatabaseConnection connect(final DataSourceCreator dataSourceFactory) {
        return new DatabaseConnectionImpl(
                dataSourceFactory.newDataSource(getJDBCUrl(), getConnectionProperties()),
                new DataSourceCloser() {
                    public void closeDataSource(DataSource dataSource) {
                        dataSourceFactory.close(dataSource);
                    }
                },
                this);
    }
    
    public Properties getConnectionProperties() {
        return new Properties();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final StandardDatabaseServer other = (StandardDatabaseServer) obj;
        if ((this.hostname == null) ? (other.hostname != null) : !this.hostname.equals(other.hostname)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if ((this.catalog == null) ? (other.catalog != null) : !this.catalog.equals(other.catalog)) {
            return false;
        }
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 41 * hash + this.port;
        hash = 41 * hash + (this.catalog != null ? this.catalog.hashCode() : 0);
        hash = 41 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 41 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getJDBCUrl();
    }
}
