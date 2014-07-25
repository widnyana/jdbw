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
package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.DataSourceFactory;
import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;

/**
 *
 * @author Martin Berglund
 */
public class H2DatabaseConnectionFactory extends AuthenticatingDatabaseConnectionFactory {
    
    private final boolean allowMultipleConnections;
    
    public H2DatabaseConnectionFactory(H2ServerType databaseServerType, String jdbcUrl, boolean allowMultipleConnections) {
        super(databaseServerType, jdbcUrl);
        this.allowMultipleConnections = allowMultipleConnections;
    }

    @Override
    public H2DatabaseConnectionFactory setUsername(String username) {
        setConnectionProperty("USER", username);
        return this;
    }

    @Override
    public H2DatabaseConnectionFactory setPassword(String password) {
        setConnectionProperty("PASSWORD", password);
        return this;
    }

    @Override
    public DatabaseConnection connect(DataSourceFactory dataSourceFactory) {
        if(allowMultipleConnections) {
            return super.connect(dataSourceFactory);
        }
        else {
            return super.connect(new OneSharedConnectionDataSource.Factory());
        }
    }
}
