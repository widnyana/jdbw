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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class TableDataStorage<U, T extends Identifiable<U>> {
    private final Class<U> keyType;
    private final Class<T> objectType;
    private final FieldMapping fieldMapping;
    private final List<Class> fieldTypes;
    private final ConcurrentHashMap<U, Object[]> keyToRowData;
    private final Map<U, T> proxyObjectMap;
    private final Map<String, Integer> fieldNameToIndexMap;

    TableDataStorage(Class<U> keyType, Class<T> objectType, FieldMapping fieldMapping) {        
        if(keyType == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null keyType");
        }
        if(objectType == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null objectType");
        }
        if(fieldMapping == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldMapping");
        }        
        this.keyType = keyType;
        this.objectType = objectType;
        this.fieldMapping = fieldMapping;
        this.fieldTypes = fieldMapping.getFieldTypes(objectType);
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

    void setRows(List<Object[]> rows) {
        Set<U> keys = new HashSet<U>();
        for(Object[] row: rows) {
            keys.add((U)row[0]);
        }
        addOrUpdateRows(rows, true);
        keyToRowData.keySet().retainAll(keys);
        synchronized(proxyObjectMap) {
            proxyObjectMap.keySet().retainAll(keys);
        }
    }

    void addOrUpdateRows(List<Object[]> rows) {
        addOrUpdateRows(rows, true);
    }
    
    private void addOrUpdateRows(List<Object[]> rows, boolean idInFront) {
        for(Object[] row: rows) {
            addOrUpdateRow(row);
        }
    }
    
    U addOrUpdateRow(Object[] row) {
        return addOrUpdateRow(row, true);
    }
    
    private U addOrUpdateRow(Object[] row, boolean idInFront) {       
        U key;
        if(idInFront) {
            key = (U)correctDatatype(row[0], keyType);
            for(int i = 1; i < row.length; i++) {
                row[i] = correctDatatype(row[i], fieldTypes.get(i - 1));
            }
        }
        else {
            key = (U)correctDatatype(row[row.length - 1], keyType);
            for(int i = 0; i < row.length - 1; i++) {
                row[i] = correctDatatype(row[i], fieldTypes.get(i));
            }
        }
        if(!keyToRowData.containsKey(key)) {
            Object[] onlyValues = new Object[row.length - 1];
            if(idInFront) {
                System.arraycopy(row, 1, onlyValues, 0, onlyValues.length);
            }
            else {
                System.arraycopy(row, 0, onlyValues, 0, onlyValues.length);
            }            
            if(keyToRowData.putIfAbsent(key, onlyValues) == null) {
                return key;
            }
        }
        if(idInFront) {
            System.arraycopy(row, 1, keyToRowData.get(key), 0, row.length - 1);
        }
        else {
            System.arraycopy(row, 0, keyToRowData.get(key), 0, row.length - 1);
        }     
        return key;
    }

    void updateRows(List<Object[]> rows) {
        addOrUpdateRows(rows, false);
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

    private Object correctDatatype(Object value, Class type) {
        if(value == null) {
            return null;
        }
        else if(value.getClass() == type) {
            return value;
        }
        else if(type.isAssignableFrom(value.getClass())) {
            return value;
        }
        else if(value instanceof String && type == UUID.class) {
            return UUID.fromString((String)value);
        }
        else if(type == int.class && value instanceof Integer) {
            return value;
        }
        else if(type == int.class &&
                (value instanceof Short || value instanceof Long)) {
            return new Integer(value.toString());
        }
        else if(type == Integer.class && 
                (value instanceof Short || value instanceof Long)) {
            return new Integer(value.toString());
        }
        else if(type == Integer.class && value instanceof BigInteger) {
            return ((BigInteger)value).intValue();
        }
        else if(type == long.class && value instanceof Long) {
            return value;
        }
        else if(type == long.class &&
                (value instanceof Short || value instanceof Integer)) {
            return new Long(value.toString());
        }
        else if(type == Long.class && 
                (value instanceof Short || value instanceof Integer)) {
            return new Long(value.toString());
        }
        else if(type == Long.class && value instanceof BigInteger) {
            return ((BigInteger)value).longValue();
        }
        else if(type == BigInteger.class && 
                (value instanceof Integer || value instanceof Long)) {
            return new BigInteger(value.toString());
        }
        else if(type == double.class && value instanceof Double) {
            return value;
        }
        else if(type == double.class && value instanceof Float) {
            return new Double(((Float)value).doubleValue());
        }
        else if(type == Double.class && value instanceof Float) {
            return ((Float)value).doubleValue();
        }
        else if(type == float.class && value instanceof Float) {
            return value;
        }
        else if(type == float.class && value instanceof Double) {
            return new Float(((Double)value).floatValue());
        }
        else if(type == Float.class && value instanceof Double) {
            return ((Double)value).floatValue();
        }
        else if(type == BigDecimal.class && value instanceof Double) {
            return new BigDecimal((Double)value);
        }
        else if(type == BigDecimal.class && value instanceof Float) {
            return new BigDecimal((Float)value);
        }
        else {
            throw new IllegalArgumentException("TableDataStorage doesn't know how to convert " +
                    value.getClass().getName() + " to " + type.getName());
        }
    }
}
