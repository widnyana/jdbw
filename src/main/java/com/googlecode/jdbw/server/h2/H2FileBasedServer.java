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
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.server.AbstractDatabaseServer;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;

/**
 *
 * @author Martin Berglund
 */
public class H2FileBasedServer extends AbstractDatabaseServer implements H2Server {
    
    private final String databaseFilePrefix;

    public H2FileBasedServer(String databaseFilePrefix) {
        super(new H2JDBCDriverDescriptor());
        this.databaseFilePrefix = databaseFilePrefix;
    }

    @Override
    protected String getJDBCUrl() {
        return ((H2JDBCDriverDescriptor)getDriverDescriptor()).formatJDBCUrlForFile(databaseFilePrefix);
    }
    
    public DatabaseServerType getServerType() {
        return DatabaseServerTypes.H2_FILE;
    }

    public DatabaseConnection connect() {
        return connect(null);
    }

    @Override
    public DatabaseConnection connect(DataSourceFactory dataSourceFactory) {
        return super.connect(new OneSharedConnectionDataSource.Factory());
    }
}
