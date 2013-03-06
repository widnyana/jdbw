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
package com.googlecode.jdbw.objectstorage;

import com.googlecode.jdbw.SQLDialect;

public interface TableMapping extends FieldMapping {
    String getTableName();
    String getColumnName(String fieldName);
    String getSelectAll(SQLDialect dialect);
    String getSelectSome(SQLDialect dialect, int numberOfObjects);
    String getSelectKeys(SQLDialect dialect, int numberOfObjects);
    String getSelectCount(SQLDialect sqlDialect);
    String getSelectContains(SQLDialect sqlDialect);
    String getInsert(SQLDialect dialect);
    String getUpdate(SQLDialect dialect);
    String getDelete(SQLDialect dialect, int numberOfObjectsToDelete);
    String getDeleteAll(SQLDialect sqlDialect);
}
