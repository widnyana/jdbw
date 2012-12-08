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
package com.googlecode.jdbw.objectstorage.impl;

import com.googlecode.jdbw.objectstorage.FieldMapping;
import com.googlecode.jdbw.objectstorage.ObjectStorageException;
import com.googlecode.jdbw.objectstorage.Storable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class ObjectProxyHandler implements InvocationHandler {
    private final FieldMapping fieldMapping;
    private final Object key;
    private final Object[] fields;

    ObjectProxyHandler(FieldMapping fieldMapping, Object key, Object[] fields) {
        this.fieldMapping = fieldMapping;
        this.key = key;
        this.fields = fields;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("getId")) {
            return getKey();
        }
        else if(fieldMapping.getFieldName(method) != null) {
            if(method.getName().startsWith("set")) {
                throw new ObjectStorageException("Error trying to modify immutable object");
            }
            return fields[fieldMapping.getFieldIndex(method)];
        }
        else if("toString".equals(method.getName())) {
            return toString();
        }
        else if("hashCode".equals(method.getName())) {
            return key.hashCode();
        }
        else if("equals".equals(method.getName()) && args != null && args.length == 1) {
            return fieldMapping.getObjectType().isAssignableFrom(args[0].getClass()) &&
                    key.equals(((Storable)args[0]).getId());
        }
        else if("storableType".equals(method.getName()) && method.getParameterTypes().length == 0) {
            return fieldMapping.getObjectType();
        }
        else {
            throw new ObjectStorageException("Unknown method call: " + fieldMapping.getObjectType().getName() + "." + method.getName());
        }
    }

    public Class<? extends Storable> getObjectType() {
        return fieldMapping.getObjectType();
    }
    
    public Object getKey() {
        return key;
    }

    protected Object[] getFields() {
        return fields;
    }

    protected FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    @Override
    public String toString() {
        return fieldMapping.getObjectType().getSimpleName() + ":" + key;
    }
}
