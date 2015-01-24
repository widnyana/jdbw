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

import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;

/**
 * Connection factory with some additional properties unique for MySQL
 * @author Martin Berglund
 */
public class MySQLDatabaseConnectionFactory extends AuthenticatingDatabaseConnectionFactory {

    MySQLDatabaseConnectionFactory(String jdbcUrl) {
        super(DatabaseServerTypes.MYSQL, jdbcUrl);
        setUseUnicode(true);
        setCharacterEncoding("utf8");
        setRewriteBatchedStatements(true);
        setContinueBatchOnError(false);
        setAllowMultiQueriesboolean(false);
        setUseCompression(true);
        setZeroDateTimeBehavior("convertToNull");
    }

    @Override
    public MySQLDatabaseConnectionFactory setUsername(String username) {
        super.setUsername(username);
        return this;
    }

    @Override
    public MySQLDatabaseConnectionFactory setPassword(String password) {
        super.setPassword(password);
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setUseUnicode(boolean useUnicode) {
        setConnectionProperty("useUnicode", useUnicode + "");
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setCharacterEncoding(String characterEncoding) {
        setConnectionProperty("characterEncoding", characterEncoding);
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setRewriteBatchedStatements(boolean rewriteBatchedStatements) {
        setConnectionProperty("rewriteBatchedStatements", rewriteBatchedStatements + "");
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setContinueBatchOnError(boolean continueBatchOnError) {
        setConnectionProperty("continueBatchOnError", continueBatchOnError + "");
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setAllowMultiQueriesboolean(boolean allowMultiQueries) {
        setConnectionProperty("allowMultiQueries", allowMultiQueries + "");
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setUseCompression(boolean useCompression) {
        setConnectionProperty("useCompression", useCompression + "");
        return this;
    }
    
    public final MySQLDatabaseConnectionFactory setZeroDateTimeBehavior(String zeroDateTimeBehavior) {
        setConnectionProperty("zeroDateTimeBehavior", zeroDateTimeBehavior);
        return this;
    }
}
