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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class TableDataStorage<U, T extends Identifiable<U>> {
    private final Class<U> keyType;
    private final FieldMapping fieldMapping;
    private final List<Class> fieldTypes;
    private final ConcurrentHashMap<U, T> proxyObjectMap;

    TableDataStorage(Class<U> keyType, FieldMapping fieldMapping) {        
        if(keyType == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null keyType");
        }
        if(fieldMapping == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldMapping");
        }        
        this.keyType = keyType;
        this.fieldMapping = fieldMapping;
        this.fieldTypes = fieldMapping.getFieldTypes();
        this.proxyObjectMap = new ConcurrentHashMap<U, T>();
    }
    
    T getProxyObject(U key) {
        return proxyObjectMap.get(key);
    }
    
    List<T> getAllProxyObjects() {
        return new ArrayList<T>(proxyObjectMap.values());
    }

    void setRows(List<Object[]> rows) {
        Set<U> keys = addOrUpdateRows(rows, true);
        proxyObjectMap.keySet().retainAll(keys);
    }

    void addOrUpdateRows(List<Object[]> rows) {
        addOrUpdateRows(rows, true);
    }
    
    private Set<U> addOrUpdateRows(List<Object[]> rows, boolean idInFront) {
        Set<U> keys = new HashSet<U>();        
        for(Object[] row: rows) {
            keys.add(addOrUpdateRow(row, idInFront));
        }
        return keys;
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
        proxyObjectMap.put(key, newProxyObject(key, row, idInFront));
        return key;
    }

    void updateRows(List<Object[]> rows) {
        addOrUpdateRows(rows, false);
    }
    
    void remove(List<U> keysOfObjectsToRemove) {
        proxyObjectMap.keySet().removeAll(keysOfObjectsToRemove);
    }
    
    public Class<T> getObjectType() {
        return fieldMapping.getObjectType();
    }

    private T newProxyObject(U key, Object[] data, boolean skipFirstElement) throws IllegalArgumentException {
        ImmutableObjectProxyHandler<U, T> proxyHandler = new ImmutableObjectProxyHandler<U, T>(fieldMapping, key, data, skipFirstElement);
        T proxyObject = (T)Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(), 
                new Class[] { getObjectType() }, 
                proxyHandler);
        return proxyObject;
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
        else if(type == boolean.class && value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        else if(type == byte.class && value instanceof Byte) {
            return ((Byte)value).byteValue();
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
        else if(Date.class.isAssignableFrom(type) && value instanceof BigDecimal) {
            DateFormat decimalDateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
            try {
                return decimalDateFormat.parse(((BigDecimal)value).toPlainString());
            }
            catch(ParseException e) {
                throw new IllegalArgumentException("TableDataStorage doesn't know how to parse "
                        + "BigDecimal value " + ((BigDecimal)value).toPlainString() + " to a Date");
            }
        }
        else {
            throw new IllegalArgumentException("TableDataStorage doesn't know how to convert " +
                    value.getClass().getName() + " to " + type.getName());
        }
    }
}
