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

import com.googlecode.jdbw.DatabaseConnectionFactory;
import com.googlecode.jdbw.JDBCDriverDescriptor;

/**
 * A common base class for {@code DatabaseServer}s with are connected to over a network. This class provides some helper 
 * methods and some default implementations for the interface methods. 
 * @author Martin Berglund
 */
public abstract class StandardDatabaseServer<T extends DatabaseConnectionFactory>
        extends AbstractDatabaseServer<T>
        implements NetworkDatabaseServer<T> {

    private final String hostname;
    private final int port;
    private final String defaultCatalog;

    protected StandardDatabaseServer(
            JDBCDriverDescriptor<T> driverDescriptor,
            String hostname, 
            int port,
            String defaultCatalog) {
        super(driverDescriptor);
        this.hostname = hostname;
        this.port = port;
        this.defaultCatalog = defaultCatalog;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
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
        return this.port == other.port;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 41 * hash + this.port;
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(hostname=" + hostname + ",port=" + port + ")";
    }
}
