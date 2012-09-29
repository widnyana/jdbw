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
import java.util.HashMap;
import java.util.Map;

class EntityProxy<U, T extends JORMEntity<U>> implements InvocationHandler {

    private final Class<T> entityClass;
    private final JORMDatabase jorm;
    private final ClassTableMapping mapping;
    
    private final U id;
    private final Map<String, Object> values;
    
    EntityProxy(Class<T> entityClass, JORMDatabase jorm, ClassTableMapping mapping, U id) {
        this.entityClass = entityClass;
        this.jorm = jorm;
        this.mapping = mapping;
        this.id = id;
        this.values = new HashMap<String, Object>();
        for(String columnName: mapping.getNonIdColumns())
            this.values.put(columnName, null);
    }

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
        else if(method.getName().equals("equals") && 
                method.getParameterTypes().length == 1 && 
                method.getParameterTypes()[0] == Object.class) {
            
            return equals(args[0]);
        }
        else if(method.getName().startsWith("get") && method.getName().length() > 3 && method.getParameterTypes().length == 0) {
            String asColumnName = Character.toLowerCase(method.getName().charAt(3)) +
                                    method.getName().substring(4);
            if(!values.containsKey(asColumnName)) {
                throw new IllegalStateException("In method call to " + entityClass.getSimpleName() + 
                        "." + method.getName() + ", couldn't find field " + asColumnName + " in " +
                        "EntityProxy");
            }
            return getValue(asColumnName);
        }
        else if(method.getName().startsWith("set") && method.getName().length() > 3 && method.getParameterTypes().length == 1) {
            String asColumnName = Character.toLowerCase(method.getName().charAt(3)) +
                                    method.getName().substring(4);
            if(!values.containsKey(asColumnName)) {
                throw new IllegalStateException("In method call to " + entityClass.getSimpleName() + 
                        "." + method.getName() + ", couldn't find field " + asColumnName + " in " +
                        "EntityProxy");
            }
            setValue(asColumnName, args[0]);
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
        String[] columnNames = mapping.getNonIdColumns();
        for(int i = 1; i < row.length; i++) {
            synchronized(values) {
                String columnName = columnNames[i - 1];
                values.put(columnName, row[i]);
            }
        }
    }
    
    synchronized Object getValue(String columnName) {
        return values.get(columnName);
    }

    private void setValue(String columnName, Object value) {
        values.put(columnName, value);
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
        return entityClass.getSimpleName().hashCode() + id.hashCode();
    }
}
