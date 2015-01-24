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

package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.SQLDialect;
import com.googlecode.jdbw.SQLExecutor;
import com.googlecode.jdbw.metadata.ServerMetaData;
import com.googlecode.jdbw.server.AbstractDatabaseType;
import java.sql.Connection;
import javax.sql.DataSource;

/**
 * DatabaseServerType implementation for MySQL/MariaDB and compatible derivatives
 * @author Martin Berglund
 */
public class MySQLServerType extends AbstractDatabaseType {
    
    public static final MySQLServerType INSTANCE = new MySQLServerType();
    
    private MySQLServerType() {
        
    }
    
    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    public SQLDialect getSQLDialect() {
        return new MySQLDialect();
    }

    @Override
    public SQLExecutor createExecutor(Connection connection) {
        return new MySQLExecutor(connection);
    }

    @Override
    public ServerMetaData createMetaDataResolver(DataSource dataSource) {
        return new MySQLMetaDataResolver(dataSource);
    }
}
