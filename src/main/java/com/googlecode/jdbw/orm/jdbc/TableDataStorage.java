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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TableDataStorage<U, T extends Identifiable<U>> {
    private final Class<T> objectType;
    private final String tableName;
    private final List<String> fieldNames;
    private final List<Class> fieldTypes;
    private final Map<U, Object[]> keyToRowData;
    private final Map<U, T> proxyObjectMap; 

    TableDataStorage(
                Class<T> objectType,
                String tableName, 
                List<String> fieldNames, 
                List<Class> fieldTypes) {
        
        if(objectType == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null objectType");
        }
        if(tableName == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null tableName");
        }
        if(fieldNames == null || fieldNames.contains(null)) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldNames "
                    + "(or list has a null element)");
        }
        if(fieldTypes == null || fieldTypes.contains(null)) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldTypes "
                    + "(or list has a null element)");
        }
        if(fieldNames.size() != fieldTypes.size()) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...), "
                    + "fieldNames has " + fieldNames.size() + " elements but fieldTypes has " 
                    + fieldTypes.size());
        }
        
        this.objectType = objectType;
        this.tableName = tableName;
        this.fieldNames = Collections.unmodifiableList(new ArrayList<String>(fieldNames));
        this.fieldTypes = Collections.unmodifiableList(new ArrayList<Class>(fieldTypes));
        this.keyToRowData = new ConcurrentHashMap<U, Object[]>();
        this.proxyObjectMap = new HashMap<U, T>();
    }
    
    T getProxyObject(U key) {
        if(!keyToRowData.containsKey(key)) {
            return null;
        }
        synchronized(proxyObjectMap) {
            if(!proxyObjectMap.containsKey(key)) {
                ImmutableObjectProxyHandler<U, T> proxyHandler = 
                        new ImmutableObjectProxyHandler<U, T>(this, key);

                T proxyObject = (T)Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(), 
                        new Class[] { objectType }, 
                        proxyHandler);

                proxyObjectMap.put(key, proxyObject);
            }
            return proxyObjectMap.get(key);
        }
    }
    
    List<T> getAllProxyObjects() {
        synchronized(proxyObjectMap) {
            return new ArrayList<T>(proxyObjectMap.values());
        }
    }

    void renewAll(List<Object[]> rows) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void renewSome(List<Object[]> rows) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void renewSome(List<Object[]> rows, boolean afterUpdate) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    void addRow(Object[] row) {
        throw new UnsupportedOperationException("Not yet implemented");
    }   
    
    void remove(List<U> keysOfObjectsToRemove) {
        throw new UnsupportedOperationException("Not yet implemented");
    }     

    public Class<T> getObjectType() {
        return objectType;
    }
}
