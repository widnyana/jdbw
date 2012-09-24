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
import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.server.AbstractDatabaseServer;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;

/**
 *
 * @author Martin Berglund
 */
public class H2InMemoryServer extends AbstractDatabaseServer implements H2Server {
    
    private final String name;

    public H2InMemoryServer() {
        this(null);
    }

    public H2InMemoryServer(String name) {
        super(new H2JDBCDriverDescriptor());
        this.name = name;
    }
    
    public DatabaseServerType getServerType() {
        return new H2ServerTypes.InMemory();
    }

    @Override
    protected String getJDBCUrl() {
        if(name == null)
            return ((H2JDBCDriverDescriptor)getDriverDescriptor()).formatJDBCUrlForAnonymousInMemory();
        else
            return ((H2JDBCDriverDescriptor)getDriverDescriptor()).formatJDBCUrlForInMemory(name);
    }
    
    public DatabaseConnection connect() {
        return connect(null);
    }

    @Override
    public DatabaseConnection connect(DataSourceFactory dataSourceFactory) {
        if(dataSourceFactory == null || name == null)
            return super.connect(new OneSharedConnectionDataSource.Factory());
        else
            return super.connect(dataSourceFactory);
    }
}
