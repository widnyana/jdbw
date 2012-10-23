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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class TableDataStorage<U, T extends Identifiable<U>> {
    private final Class<T> objectType;
    private final FieldMapping fieldMapping;
    private final ConcurrentHashMap<U, Object[]> keyToRowData;
    private final Map<U, T> proxyObjectMap;
    private final Map<String, Integer> fieldNameToIndexMap;

    TableDataStorage(Class<T> objectType, FieldMapping fieldMapping) {        
        if(objectType == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null objectType");
        }
        if(fieldMapping == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldMapping");
        }        
        this.objectType = objectType;
        this.fieldMapping = fieldMapping;
        this.keyToRowData = new ConcurrentHashMap<U, Object[]>();
        this.proxyObjectMap = new HashMap<U, T>();
        this.fieldNameToIndexMap = new HashMap<String, Integer>(fieldMapping.getFieldNames(objectType).size());
        int index = 0;
        for(String fieldName: fieldMapping.getFieldNames(objectType)) {
            fieldNameToIndexMap.put(fieldName, index++);
        }
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
            if(proxyObjectMap.size() == keyToRowData.size()) {
                return new ArrayList<T>(proxyObjectMap.values());
            }
        }
        List<T> proxies = new ArrayList<T>();
        for(U key: keyToRowData.keySet()) {
            proxies.add(getProxyObject(key));
        }
        return proxies;
    }

    void renewAll(List<Object[]> rows) {
        Set<U> keys = new HashSet<U>();
        for(Object[] row: rows) {
            keys.add((U)row[0]);
        }
        renewSome(rows);
        keyToRowData.keySet().retainAll(keys);
        synchronized(proxyObjectMap) {
            proxyObjectMap.keySet().retainAll(keys);
        }
    }

    void renewSome(List<Object[]> rows) {
        for(Object[] row: rows) {
            U key = (U)row[0];
            if(!keyToRowData.containsKey(key)) {
                Object[] onlyValues = new Object[row.length - 1];
                System.arraycopy(row, 1, onlyValues, 0, onlyValues.length);
                if(keyToRowData.putIfAbsent((U)row[0], onlyValues) == null) {
                    continue;
                }
            }
            System.arraycopy(row, 1, keyToRowData.get(key), 0, row.length - 1);
        }
    }

    void renewSome(List<Object[]> rows, boolean afterUpdate) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    void addRow(Object[] row) {
        throw new UnsupportedOperationException("Not yet implemented");
    }   
    
    void remove(List<U> keysOfObjectsToRemove) {
        keyToRowData.keySet().removeAll(keysOfObjectsToRemove);
        synchronized(proxyObjectMap) {
            proxyObjectMap.keySet().removeAll(keysOfObjectsToRemove);
        }
    }     

    Object getValue(U key, String methodName) {
        Object[] data = keyToRowData.get(key);
        if(data == null) {
            throw new RuntimeException("Object " + objectType.getSimpleName() + ":" + key + " was deleted");
        }
        String fieldName = fieldMapping.getFieldName(objectType, methodName);
        if(!fieldNameToIndexMap.containsKey(fieldName)) {
            throw new RuntimeException("Unexpectedly didn't know about field " + objectType.getSimpleName() +
                    "." + fieldName);
        }
        return data[fieldNameToIndexMap.get(fieldName)];
    }
    
    public Class<T> getObjectType() {
        return objectType;
    }
}
