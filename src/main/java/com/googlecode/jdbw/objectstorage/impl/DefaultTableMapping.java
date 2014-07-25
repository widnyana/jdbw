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
package com.googlecode.jdbw.objectstorage.impl;

import com.googlecode.jdbw.SQLDialect;
import com.googlecode.jdbw.objectstorage.Storable;
import com.googlecode.jdbw.objectstorage.TableMapping;

public class DefaultTableMapping extends DefaultFieldMapping implements TableMapping {
    
    private final String tableName;
    
    public DefaultTableMapping(Class<? extends Storable> objectType) {
        this(objectType, objectType.getSimpleName());
    }
    
    public DefaultTableMapping(Class<? extends Storable> objectType, String tableName) {
        super(objectType);
        this.tableName = tableName;
    }
    
    @Override
    public String getColumnName(String fieldName) {
        return fieldName;
    }

    @Override
    public String getTableName() {
        return tableName;
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
        if(numberOfObjects <= 0) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getSelectSome with numberOfObjects <= 0 ");
        }
        StringBuilder sb = new StringBuilder("SELECT ");
        String idAsEscapedColumnName = dialect.escapeIdentifier(getColumnName("id"));
        sb.append(idAsEscapedColumnName);
        for(String fieldName: getFieldNames()) {
            sb.append(", ").append(dialect.escapeIdentifier(getColumnName(fieldName)));
        }
        sb.append(" FROM ").append(dialect.escapeIdentifier(getTableName()));
        sb.append(" WHERE ").append(idAsEscapedColumnName).append(" IN (?");
        for(int i = 1; i < numberOfObjects; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getSelectKeys(SQLDialect dialect, int numberOfObjects) {
        if(numberOfObjects <= 0) {
            throw new IllegalArgumentException("Cannot call DefaultTableMapping.getSelectKeys with numberOfObjects <= 0 ");
        }
        StringBuilder sb = new StringBuilder("SELECT ");
        String idAsEscapedColumnName = dialect.escapeIdentifier(getColumnName("id"));
        sb.append(idAsEscapedColumnName);
        sb.append(" FROM ").append(dialect.escapeIdentifier(getTableName()));
        sb.append(" WHERE ").append(idAsEscapedColumnName).append(" IN (?");
        for(int i = 1; i < numberOfObjects; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getSelectCount(SQLDialect sqlDialect) {
        return "SELECT COUNT(" + sqlDialect.escapeIdentifier("id") + ") FROM " + sqlDialect.escapeIdentifier(getTableName());
    }

    @Override
    public String getSelectContains(SQLDialect sqlDialect) {
        return "SELECT COUNT(" + sqlDialect.escapeIdentifier("id") + ") FROM " +
                sqlDialect.escapeIdentifier(getTableName()) + " WHERE " +
                sqlDialect.escapeIdentifier("id") + " = ?";
    }

    @Override
    public String getInsert(SQLDialect dialect) {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" (");
        sb.append(dialect.escapeIdentifier(getColumnName("id")));
        for(String fieldName: getFieldNames()) {
            sb.append(", ").append(dialect.escapeIdentifier(getColumnName(fieldName)));
        }
        sb.append(") VALUES(?");
        for(int i = 0; i < getFieldNames().size(); i++) {
            sb.append(", ?");
        }
        return sb.append(")").toString();
    }

    @Override
    public String getUpdate(SQLDialect dialect) {
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" SET ");
        for(String fieldName: getFieldNames()) {
            sb.append(dialect.escapeIdentifier(getColumnName(fieldName))).append(" = ?, ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" WHERE ");
        sb.append(dialect.escapeIdentifier(getColumnName("id")));
        return sb.append(" = ?").toString();
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

    @Override
    public String getDeleteAll(SQLDialect dialect) {
        return "DELETE FROM " + dialect.escapeIdentifier(getTableName());
    }
}
