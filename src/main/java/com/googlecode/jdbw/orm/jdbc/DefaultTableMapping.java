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

public class DefaultTableMapping extends DefaultFieldMapping implements TableMapping {

    @Override
    public <U, T extends Identifiable<U>> String getTableName(Class<T> objectType) {
        return objectType.getSimpleName();
    }

    @Override
    public <U, T extends Identifiable<U>> String getColumnName(Class<T> objectType, String fieldName) {
        return fieldName;
    }

    @Override
    public <U, T extends Identifiable<U>> String getSelectAll(SQLDialect dialect, Class<T> objectType) {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(dialect.escapeIdentifier("id"));
        for(String fieldName: getFieldNames(objectType)) {
            sb.append(", ").append(dialect.escapeIdentifier(getColumnName(objectType, fieldName)));
        }
        sb.append(" FROM ").append(dialect.escapeIdentifier(getTableName(objectType)));
        return sb.toString();
    }

    @Override
    public <U, T extends Identifiable<U>> String getSelectSome(SQLDialect dialect, Class<T> objectType, List<U> keys) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getSelectSome(...) with null objectType");
        }
        if(keys.isEmpty()) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getSelectSome(...) with no keys");
        }
        StringBuilder sb = new StringBuilder(getSelectAll(dialect, objectType));
        sb.append(" WHERE ");
        sb.append(dialect.escapeIdentifier("id"));
        sb.append(" IN (?");
        for(int i = 1; i < keys.size(); i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public <U, T extends Identifiable<U>> String getInsert(SQLDialect dialect, Class<T> objectType) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getInsert(...) with null objectType");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(dialect.escapeIdentifier(getTableName(objectType)));
        sb.append(" (").append(dialect.escapeIdentifier("id"));
        for(String fieldName: getFieldNames(objectType)) {
            sb.append(", ").append(dialect.escapeIdentifier(fieldName));
        }
        sb.append(") VALUES(?");
        for(String fieldName: getFieldNames(objectType)) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public <U, T extends Identifiable<U>> String getUpdate(SQLDialect dialect, Class<T> objectType) {
        if(dialect == null) {
            dialect = new DefaultSQLDialect();
        }
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getUpdate(...) with null objectType");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(dialect.escapeIdentifier(getTableName(objectType)));
        sb.append(" SET ");
        for(String fieldName: getFieldNames(objectType)) {
            sb.append(dialect.escapeIdentifier(fieldName)).append(" = ?, ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" WHERE ");
        sb.append(dialect.escapeIdentifier("id")).append(" = ?");
        return sb.toString();
    }

    @Override
    public <U, T extends Identifiable<U>> String getDelete(SQLDialect dialect, Class<T> objectType, int numberOfObjectsToDelete) {
        if(numberOfObjectsToDelete <= 0) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getDelete(...) with numberOfObjectsToDelete <= 0");
        }
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(dialect.escapeIdentifier(getTableName(objectType)));
        sb.append(" WHERE ").append(dialect.escapeIdentifier("id")).append(" IN (?");
        for(int i = 1; i < numberOfObjectsToDelete; i++) {
            sb.append(", ?");
        }
        return sb.append(")").toString();
    }
    
}
