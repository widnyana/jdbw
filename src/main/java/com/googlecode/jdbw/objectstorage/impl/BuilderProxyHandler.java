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
import com.googlecode.jdbw.objectstorage.ObjectFactory;
import com.googlecode.jdbw.objectstorage.ObjectStorageException;
import java.lang.reflect.Method;

class BuilderProxyHandler extends ObjectProxyHandler {
    
    private final ObjectFactory objectFactory;

    BuilderProxyHandler(FieldMapping fieldMapping, ObjectFactory objectFactory, Object key) {
        super(fieldMapping, key, new Object[fieldMapping.getFieldNames().size()]);
        this.objectFactory = objectFactory;
    }
    
    /*
    public BuilderProxyHandler(FieldMapping fieldMapping, Object key) {
    }
    */
    
    BuilderProxyHandler(FieldMapping fieldMapping, ObjectFactory objectFactory, Object key, Object template) {
        this(fieldMapping, objectFactory, key);
        for(Method method: fieldMapping.getObjectType().getMethods()) {
            if(fieldMapping.getFieldName(method) == null || "getId".equals(method.getName())) {
                continue;
            }
            try {
                method.setAccessible(true);
                setFieldValue(method, method.invoke(template));
            }
            catch(Exception e) {
                throw new ObjectStorageException("Couldn't copy value from template due to " + e.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().startsWith("set") && method.getName().length() > 3) {
            setFieldValue(method, args[0]);
            return proxy;
        }
        else if("build".equals(method.getName())) {
            FieldMapping fieldMapping = getFieldMapping();
            Object[] idAndValues = new Object[getFields().length + 1];
            idAndValues[0] = getKey();
            System.arraycopy(getFields(), 0, idAndValues, 1, getFields().length);
            return objectFactory.newObject(fieldMapping.getObjectType(), fieldMapping, idAndValues);
        }
        else {
            return super.invoke(proxy, method, args);
        }
    }

    @Override
    public String toString() {
        return "Builder:" + super.toString();
    }

    private void setFieldValue(Method method, Object value) {
        getFields()[getFieldMapping().getFieldIndex(method)] = value;
    }
}
