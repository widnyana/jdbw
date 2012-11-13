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
package com.googlecode.jdbw.orm.jdbc;

import com.googlecode.jdbw.SQLDialect;
import com.googlecode.jdbw.orm.Identifiable;
import com.googlecode.jdbw.server.DefaultSQLDialect;
import java.util.List;

public class DefaultTableMapping<T extends Identifiable> extends DefaultFieldMapping<T> implements TableMapping<T> {

    public DefaultTableMapping(Class<T> objectType) {
        super(objectType);
    }
    
    @Override
    public String getTableName() {
        return getObjectType().getSimpleName();
    }

    @Override
    public String getColumnName(String fieldName) {
        return fieldName;
    }

    @Override
    public String getSelectAll(SQLDialect dialect) {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(dialect.escapeIdentifier("id"));
        for(String fieldName: getFieldNames()) {
            sb.append(", ").append(dialect.escapeIdentifier(getColumnName(fieldName)));
        }
        sb.append(" FROM ").append(dialect.escapeIdentifier(getTableName()));
        return sb.toString();
    }

    @Override
    public String getSelectSome(SQLDialect dialect, int numberOfObjects) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        if(numberOfObjects <= 0) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getSelectSome(...) with numberOfObjects <= 0");
        }
        StringBuilder sb = new StringBuilder(getSelectAll(dialect));
        sb.append(" WHERE ");
        sb.append(dialect.escapeIdentifier("id"));
        sb.append(" IN (?");
        for(int i = 1; i < numberOfObjects; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getInsert(SQLDialect dialect) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" (").append(dialect.escapeIdentifier("id"));
        for(String fieldName: getFieldNames()) {
            sb.append(", ").append(dialect.escapeIdentifier(fieldName));
        }
        sb.append(") VALUES(?");
        for(String fieldName: getFieldNames()) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getUpdate(SQLDialect dialect) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" SET ");
        for(String fieldName: getFieldNames()) {
            sb.append(dialect.escapeIdentifier(fieldName)).append(" = ?, ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" WHERE ");
        sb.append(dialect.escapeIdentifier("id")).append(" = ?");
        return sb.toString();
    }

    @Override
    public String getDelete(SQLDialect dialect, int numberOfObjectsToDelete) {
        if(numberOfObjectsToDelete <= 0) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getDelete(...) with numberOfObjectsToDelete <= 0");
        }
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" WHERE ").append(dialect.escapeIdentifier("id")).append(" IN (?");
        for(int i = 1; i < numberOfObjectsToDelete; i++) {
            sb.append(", ?");
        }
        return sb.append(")").toString();
    }
    
}
