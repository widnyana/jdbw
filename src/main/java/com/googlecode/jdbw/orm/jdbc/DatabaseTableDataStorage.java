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

import com.googlecode.jdbw.orm.Identifiable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatabaseTableDataStorage {
    private final Map<Class, TableDataStorage> tables;

    DatabaseTableDataStorage() {
        tables = new HashMap<Class, TableDataStorage>();
    }
    
    <U, T extends Identifiable<U>> TableDataStorage<U, T> get(Class<T> objectType) {
        synchronized(tables) {
            return (TableDataStorage<U, T>)tables.get(objectType);
        }
    }
    
    <U, T extends Identifiable<U>> void add(Class<T> objectType, Class<U> idType, FieldMapping fieldMapping) {
        synchronized(tables) {
            if(!tables.containsKey(objectType)) {
                tables.put(objectType, new TableDataStorage<U, T>(idType, objectType, fieldMapping));
            }
        }
    }
    
    List<Class<? extends Identifiable>> getAllObjectTypes() {
        synchronized(tables) {
            return new ArrayList<Class<? extends Identifiable>>((Collection)tables.keySet());
        }
    }
}
