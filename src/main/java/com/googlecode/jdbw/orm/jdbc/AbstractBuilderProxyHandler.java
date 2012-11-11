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
import com.googlecode.jdbw.orm.Modifiable;
import com.googlecode.jdbw.orm.Persistable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractBuilderProxyHandler<U, T extends Identifiable<U> & Modifiable> extends CommonProxyHandler<U, T> {
    
    private final FieldMapping fieldMapping;
    private final Class<T> objectType;
    private final Map<String, Object> values;

    public AbstractBuilderProxyHandler(
                FieldMapping fieldMapping, 
                Class<T> objectType, 
                U key,
                Map<String, Object> initialValues) {
        
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot create ModifiableObjectProxyHandler with null objectType");
        }
        this.fieldMapping = fieldMapping;
        this.values = new HashMap<String, Object>(initialValues);
        this.objectType = objectType;
        
        values.put("id", key);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if((method.getName().startsWith("get") && method.getName().length() > 3) ||
                (method.getName().startsWith("is") && method.getName().length() > 2)) {
            return values.get(fieldMapping.getFieldName(method.getName()));
        }
        else if(method.getName().startsWith("set") && method.getName().length() > 3 && 
                method.getParameterTypes().length == 1) {
            setValue(fieldMapping.getFieldName(method.getName()), args[0]);
            return proxy;
        }
        else if(method.getName().equals("build") && method.getParameterTypes().length == 0) {
            return makeFinalizedVersion();
        }
        else if(method.getName().equals("modify") && method.getParameterTypes().length == 0) {
            return makeCopyOfThis(fieldMapping, objectType, values);
        }
        else if(method.getName().equals("toString") && method.getParameterTypes().length == 0) {
            return toString();
        }
        else if(method.getName().equals("hashCode") && method.getParameterTypes().length == 0) {
            return hashCode();
        }
        else if(method.getName().equals("equals") && 
                method.getParameterTypes().length == 1 &&
                method.getParameterTypes()[0] == Object.class) {
            return equals(args[0]);
        }
        else {
            throw new UnsupportedOperationException("Unsupported method call: " + method.toString());
        }
    }

    @Override
    Class<T> getObjectType() {
        return objectType;
    }

    @Override
    U getKey() {
        return (U)values.get("id");
    }
    
    protected void setValue(String fieldName, Object value) {
        if(fieldName == null) {
            throw new IllegalArgumentException("Cannot set field 'null'");
        }
        values.put(fieldName, value);
    }
    
    protected Object[] copyValuesToArray(boolean idFirst) {
        List<String> fieldNames = fieldMapping.getFieldNames();
        Object[] array = new Object[fieldNames.size() + 1];
        int firstIndex = 0;
        if(idFirst) {
            array[0] = getKey();
            firstIndex = 1;
        }
        for(int i = 0; i < fieldNames.size(); i++) {
            array[i + firstIndex] = values.get(fieldNames.get(i));
        }
        if(!idFirst) {
            array[array.length - 1] = getKey();
        }
        return array;
    }

    protected abstract Object makeCopyOfThis(FieldMapping fieldMapping, Class<T> objectType, Map<String, Object> values);

    protected abstract <V extends Finalized<U, T>> V makeFinalizedVersion();
    
    abstract static class Finalized<U, T extends Identifiable<U> & Modifiable> implements Persistable<U, T> {
        
        private final Class<T> objectType;
        private final U id;
        private final Object[] values;

        public Finalized(Class<T> objectType, U id, Object[] values) {
            this.objectType = objectType;
            this.id = id;
            this.values = values;
        }
        
        @Override
        public U getId() {
            return id;
        }

        @Override
        public Class<T> getObjectType() {
            return objectType;
        }

        Object[] getValues() {
            return values;
        }
    } 
}
