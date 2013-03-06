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
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class is available for extending, delegating all method calls to a another
 * TableMappingFactory so that you can extend only the methods you are interested in.
 * @author mberglun
 */
public class DelegatingTableMapping implements TableMapping {
    private final TableMapping backend;

    public DelegatingTableMapping(TableMapping backend) {
        this.backend = backend;
    }

    @Override
    public String getTableName() {
        return backend.getTableName();
    }

    @Override
    public String getColumnName(String fieldName) {
        return backend.getColumnName(fieldName);
    }

    @Override
    public String getSelectAll(SQLDialect dialect) {
        return backend.getSelectAll(dialect);
    }

    @Override
    public String getSelectSome(SQLDialect dialect, int numberOfObjects) {
        return backend.getSelectSome(dialect, numberOfObjects);
    }

    @Override
    public String getSelectKeys(SQLDialect dialect, int numberOfObjects) {
        return backend.getSelectKeys(dialect, numberOfObjects);
    }

    @Override
    public String getSelectCount(SQLDialect sqlDialect) {
        return backend.getSelectCount(sqlDialect);
    }

    @Override
    public String getSelectContains(SQLDialect sqlDialect) {
        return backend.getSelectContains(sqlDialect);
    }

    @Override
    public String getInsert(SQLDialect dialect) {
        return backend.getInsert(dialect);
    }

    @Override
    public String getUpdate(SQLDialect dialect) {
        return backend.getUpdate(dialect);
    }

    @Override
    public String getDelete(SQLDialect dialect, int numberOfObjectsToDelete) {
        return backend.getDelete(dialect, numberOfObjectsToDelete);
    }

    @Override
    public String getDeleteAll(SQLDialect sqlDialect) {
        return backend.getDeleteAll(sqlDialect);
    }

    @Override
    public Class<? extends Storable> getObjectType() {
        return backend.getObjectType();
    }

    @Override
    public String getFieldName(String methodName) {
        return backend.getFieldName(methodName);
    }

    @Override
    public String getFieldName(Method method) {
        return backend.getFieldName(method);
    }

    @Override
    public int getFieldIndex(String fieldName) {
        return backend.getFieldIndex(fieldName);
    }

    @Override
    public int getFieldIndex(Method method) {
        return backend.getFieldIndex(method);
    }

    @Override
    public List<String> getFieldNames() {
        return backend.getFieldNames();
    }

    @Override
    public List<Class> getFieldTypes() {
        return backend.getFieldTypes();
    }
}
