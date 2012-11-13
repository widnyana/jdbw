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
import com.googlecode.jdbw.orm.ObjectBuilder;
import com.googlecode.jdbw.orm.ObjectStorageException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DefaultFieldMapping<T extends Identifiable> implements FieldMapping<T> {
    
    private final Class<T> objectType;
    private final Class builderInterface;
    private final Map<String, Integer> fieldIndexMap;
    private final SortedMap<String, Class> fieldTypeMap;

    public DefaultFieldMapping(Class<T> objectType) {
        this.objectType = objectType;
        this.fieldIndexMap = new HashMap<String, Integer>();
        this.fieldTypeMap = new TreeMap<String, Class>();
        this.builderInterface = resolveBuilder();
        
        resolveFields();
    }
    
    private Class resolveBuilder() {
        if(!Modifiable.class.isAssignableFrom(objectType)) {
            return null;
        }
        for(Type type: objectType.getGenericInterfaces()) {
            if(type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if(parameterizedType.getRawType() == Modifiable.class) {
                    if(ObjectBuilder.class.isAssignableFrom((Class)parameterizedType.getActualTypeArguments()[0])) {
                        return (Class)parameterizedType.getActualTypeArguments()[0];
                    }
                }                
            }
        }
        throw new ObjectStorageException("Unknown builder class for " + objectType.getSimpleName(), new Exception());
    }
    
    private void resolveFields() {
        for(Method method: objectType.getMethods()) {
            if((method.getModifiers() & Modifier.STATIC) != 0)
                continue;
            
            String fieldName = null;
            if(method.getName().startsWith("get")) {
                if(method.getName().equals("getId") || method.getName().length() <= 3) {
                    continue;
                }
                //Found a field!
                fieldName = getFieldName(method.getName());
            }
            else if(method.getName().startsWith("is")) {
                if(method.getName().length() <= 2) {
                    continue;
                }
                //Found a field!
                fieldName = getFieldName(method.getName());
            }
            
            if(fieldName != null) {
                fieldTypeMap.put(fieldName, method.getReturnType());
            }
        }
        
        int index = 0;
        for(String fieldName: fieldTypeMap.keySet()) {
            fieldIndexMap.put(fieldName, index++);
        }
    }

    @Override
    public Class<T> getObjectType() {
        return objectType;
    }

    @Override
    public Class getBuilderInterface() {
        return builderInterface;
    }

    @Override
    public String getFieldName(String methodName) {
        if(methodName.startsWith("get") || methodName.startsWith("set")) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        else if(methodName.startsWith("is")) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        throw new IllegalArgumentException("Cannot detect field name for " + methodName + ", unknown format");
    }

    @Override
    public int getFieldIndex(String methodName) {
        return fieldIndexMap.get(getFieldName(methodName));
    }
    
    @Override
    public List<String> getFieldNames() {
        return new ArrayList<String>(fieldTypeMap.keySet());
    }

    @Override
    public List<Class> getFieldTypes() {
        return new ArrayList<Class>(fieldTypeMap.values());
    }
}
