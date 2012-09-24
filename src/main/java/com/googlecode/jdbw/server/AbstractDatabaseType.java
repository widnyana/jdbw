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
import com.googlecode.jdbw.impl.SQLExecutorImpl;
import com.googlecode.jdbw.metadata.MetaDataResolver;
import java.sql.*;
import javax.sql.DataSource;

/**
 * Includes some fundamental checks for connection errors and default 
 * implementations for some JDBW objects
 * @author Martin Berglund
 */
public abstract class AbstractDatabaseType implements DatabaseServerType {

    public SQLDialect getSQLDialect() {
        return new DefaultSQLDialect();
    }
    
    @Override
    public AutoExecutor createAutoExecutor(DataSource dataSource) {
        return new AutoExecutor(dataSource, this);
    }

    @Override
    public SQLExecutor createExecutor(Connection connection) {
        return new SQLExecutorImpl(connection) {};
    }

    @Override
    public MetaDataResolver createMetaDataResolver(DataSource dataSource) {
        return new MetaDataResolver(dataSource);
    }
    
    @Override
    public boolean isConnectionError(SQLException e)
    {
        if(e instanceof SQLTransientException)
            return true;
        if(e instanceof SQLNonTransientException)
            return true;    //Try again...
        if(e instanceof SQLRecoverableException)
            return true;
        
        if(e instanceof SQLSyntaxErrorException)
            return false;

        //Other than that, dunno...! You'll have to implement this for every database vendor!
        return false;
    }
}
