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
    public <U, T extends Identifiable<U>> String getSelectAll(SQLDialect dialect, Class<T> objectType, TableMapping tableMapping) {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(dialect.escapeIdentifier("id"));
        for(String fieldName: tableMapping.getFieldNames(objectType)) {
            sb.append(", ").append(dialect.escapeIdentifier(tableMapping.getColumnName(objectType, fieldName)));
        }
        sb.append(" FROM ").append(dialect.escapeIdentifier(tableMapping.getTableName(objectType)));
        return sb.toString();
    }

    @Override
    public <U, T extends Identifiable<U>> String getSelectSome(SQLDialect dialect, Class<T> objectType, TableMapping tableMapping, List<U> keys) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> String getInsert(SQLDialect dialect, Class<T> objectType, TableMapping tableMapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> String getUpdate(SQLDialect dialect, Class<T> objectType, TableMapping tableMapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> String getDelete(SQLDialect dialect, Class<T> objectType, TableMapping tableMapping, int numberOfObjectsToDelete) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
