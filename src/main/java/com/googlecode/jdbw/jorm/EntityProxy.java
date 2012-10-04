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
package com.googlecode.jdbw.jorm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class EntityProxy<U, T extends JORMEntity<U>> implements InvocationHandler, Persistable<T> {

    private static final Map<Class, Map<String, Integer>> INDEX_REFERENCE = new HashMap<Class, Map<String, Integer>>();
    
    private static <U, T extends JORMEntity<U>> void indexClass(Class<T> entityClass, ClassTableMapping classTableMapping) {
        synchronized(INDEX_REFERENCE) {
            if(INDEX_REFERENCE.containsKey(entityClass))
                return;
            
            Map<String, Integer> classFieldMap = new HashMap<String, Integer>();
            int counter = 0;
            for(String fieldName: classTableMapping.getFieldNames(entityClass)) {
                classFieldMap.put(fieldName, counter++);
            }
            INDEX_REFERENCE.put(entityClass, classFieldMap);
        }
    }
    
    private static <U, T extends JORMEntity<U>> int getNumberOfFields(Class<T> entityClass) {
        synchronized(INDEX_REFERENCE) {
            return INDEX_REFERENCE.get(entityClass).size();
        }
    }
    
    private static <U, T extends JORMEntity<U>> int getFieldIndex(Class<T> entityClass, String field) {
        synchronized(INDEX_REFERENCE) {
            return INDEX_REFERENCE.get(entityClass).containsKey(field) ? 
                        INDEX_REFERENCE.get(entityClass).get(field) : -1;
        }
    }
        
    private final Class<T> entityClass;  
    private U id;
    private T object;
    private final Object[] values;
    
    EntityProxy(Class<T> entityClass, ClassTableMapping mapping, U id, Object[] initData) {
        indexClass(entityClass, mapping);
        this.entityClass = entityClass;
        this.id = id;
        this.object = null;  //This should be setup as quickly as possible by JORMDatabase.newEntityProxy
        this.values = Arrays.copyOf(initData, getNumberOfFields(entityClass));
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("getId") && args == null) {
            return id;
        }
        else if(method.getName().equals("__underlying_proxy") && args == null) {
            return this;
        }
        else if(method.getName().equals("toString") && args == null) {
            return toString();
        }
        else if(method.getName().equals("hashCode") && args == null) {
            return hashCode();
        }
        else if(method.getName().equals("finish") && args == null) {
            return this;
        }
        else if(method.getName().equals("equals") && 
                method.getParameterTypes().length == 1 && 
                method.getParameterTypes()[0] == Object.class) {
            
            return equals(args[0]);
        }
        else if((method.getName().startsWith("get") && method.getName().length() > 3 && method.getParameterTypes().length == 0) ||
                    (method.getName().startsWith("is") && method.getName().length() > 2 && method.getParameterTypes().length == 0)) {
            
            String asFieldName;
            if(method.getName().startsWith("get"))
                asFieldName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            else
                asFieldName = Character.toLowerCase(method.getName().charAt(2)) + method.getName().substring(3);
            
            if(getFieldIndex(entityClass, asFieldName) == -1) {
                throw new IllegalStateException("In method call to " + entityClass.getSimpleName() + 
                        "." + method.getName() + ", couldn't find field " + asFieldName + " in " +
                        "EntityProxy");
            }
            return JORMDatabase.convertToReturnType(method.getReturnType(), getValue(asFieldName));
        }
        else if(method.getName().startsWith("set") && method.getName().length() > 3 && method.getParameterTypes().length == 1) {
            String asFieldName = Character.toLowerCase(method.getName().charAt(3)) +
                                    method.getName().substring(4);
            if(getFieldIndex(entityClass, asFieldName) == -1) {
                throw new IllegalStateException("In method call to " + entityClass.getSimpleName() + 
                        "." + method.getName() + ", couldn't find field " + asFieldName + " in " +
                        "EntityProxy");
            }
            setValue(asFieldName, args[0]);
            if(method.getReturnType() != Void.class)
                return proxy;
            else
                return null;
        }
        else {
            throw new IllegalArgumentException("EntityProxy doesn't know how to handle a call to " +
                    entityClass.getSimpleName() + "." + method.getName());
        }
    }

    synchronized void populate(Object[] row) {
        System.arraycopy(row, 1, values, 0, values.length); //Remember, the id is at index 0, we don't want to copy that!
    }

    void setId(U id) {
        this.id = id;
    }
    
    U getId() {
        return id;
    }

    void setObject(T object) {
        this.object = object;
    }

    T getObject() {
        return object;
    }
    
    synchronized Object getValue(String columnName) {
        return values[getFieldIndex(entityClass, columnName)];
    }

    private void setValue(String columnName, Object value) {
        values[getFieldIndex(entityClass, columnName)] = value;
    }
    
    Class<T> getEntityType() {
        return entityClass;
    }
    
    static interface Resolver<U, T extends JORMEntity<U>> {
        EntityProxy<U, T> __underlying_proxy();
    }

    @Override
    public synchronized String toString() {
        return entityClass.getSimpleName() + "(id=" + id + ")" + values;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(this == obj) {
            return true;
        }
        if(id == null) {
            return super.equals(obj);
        }
        if(!entityClass.isAssignableFrom(obj.getClass()) &&
                obj.getClass() != getClass()) {
            return false;
        }
        
        U otherId;
        if(entityClass.isAssignableFrom(obj.getClass())) {
            otherId = ((JORMEntity<U>)obj).getId();
        }
        else {
            final EntityProxy<U, T> other = (EntityProxy<U, T>) obj;
            if(this.entityClass != other.entityClass && (this.entityClass == null || !this.entityClass.equals(other.entityClass))) {
                return false;
            }
            otherId = other.id;
        }        
        if(this.id != otherId && (this.id == null || !this.id.equals(otherId))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if(id != null) {
            return entityClass.getSimpleName().hashCode() + id.hashCode();
        }
        else {
            return super.hashCode();
        }
    }
}
