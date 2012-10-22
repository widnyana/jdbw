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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class DefaultFieldMapping implements FieldMapping {
    
    private <U, T extends Identifiable<U>> SortedMap<String, Class> getFieldNamesAndTypes(Class<T> objectType) {
        SortedMap<String, Class> fields = new TreeMap<String, Class>();
        for(Method method: objectType.getMethods()) {
            if((method.getModifiers() & Modifier.STATIC) != 0)
                continue;
            
            String fieldName = null;
            if(method.getName().startsWith("get")) {
                if(method.getName().equals("getId") || method.getName().length() <= 3) {
                    continue;
                }
                //Found a field!
                fieldName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            }
            else if(method.getName().startsWith("is")) {
                if(method.getName().length() <= 2) {
                    continue;
                }
                //Found a field!
                fieldName = Character.toLowerCase(method.getName().charAt(2)) + method.getName().substring(3);
            }
            
            if(fieldName != null) {
                fields.put(fieldName, method.getReturnType());
            }
        }
        return fields;
    }
    
    @Override
    public <U, T extends Identifiable<U>> List<String> getFieldNames(Class<T> objectType) {
        return new ArrayList<String>(getFieldNamesAndTypes(objectType).keySet());
    }

    @Override
    public <U, T extends Identifiable<U>> List<Class> getFieldTypes(Class<T> objectType) {
        return new ArrayList<Class>(getFieldNamesAndTypes(objectType).values());
    }

    @Override
    public <U, T extends Identifiable<U>> Map<String, Object> getFieldValues(Class<T> objectType, T object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> Object[] getFieldValuesNoId(Class<T> objectType, T object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> Object[] getFieldValuesLeadingId(Class<T> objectType, T object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <U, T extends Identifiable<U>> Object[] getFieldValuesTrailingId(Class<T> objectType, T object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
