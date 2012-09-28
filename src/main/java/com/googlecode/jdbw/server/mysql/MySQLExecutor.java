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

import com.googlecode.jdbw.impl.SQLExecutorImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Special MySQL SQL executor that makes sure results are coming back streaming
 * (instead of pre-caching the whole result) and also disables generated keys
 * retrieval for batch queries (this causes errors otherwise).
 * 
 * @author Martin Berglund
 */
class MySQLExecutor extends SQLExecutorImpl {

    MySQLExecutor(Connection connection) {
        super(connection);
    }

    @Override
    protected PreparedStatement prepareExecuteStatement(String SQL) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        ps.setFetchSize(Integer.MIN_VALUE);
        return ps;
    }

    @Override
    protected PreparedStatement prepareQueryStatement(String SQL) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        ps.setFetchSize(Integer.MIN_VALUE);
        return ps;
    }

    @Override
    protected PreparedStatement prepareInsertStatement(String SQL) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        return ps;
    }

    @Override
    protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
        return ps;
    }

    @Override
    protected void executeUpdate(Statement statement, String SQL) throws SQLException {
        super.executeUpdate(statement, SQL);
    }
    
}
