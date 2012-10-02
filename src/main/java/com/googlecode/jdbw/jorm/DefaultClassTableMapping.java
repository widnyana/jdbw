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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DefaultClassTableMapping implements ClassTableMapping {
    
    private final String prefix;
    private final String postfix;

    public DefaultClassTableMapping() {
        this("");
    }
    
    public DefaultClassTableMapping(String prefix) {
        this(prefix, "");
    }

    public DefaultClassTableMapping(String prefix, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }

    @Override
    public <U, T extends JORMEntity<U>> String getTableName(Class<T> entityType) {
        return prefix + entityType.getSimpleName() + postfix; 
    }

    @Override
    public <U, T extends JORMEntity<U>> List<String> getFieldNames(Class<T> entityType) {
        Set<String> fields = new TreeSet<String>();
        for(Method method: entityType.getMethods()) {
            if((method.getModifiers() & Modifier.STATIC) != 0)
                continue;
            if(!method.getName().startsWith("get") || method.getName().length() <= 3)
                continue;
            if(method.getName().equals("getId"))
                continue;
            
            String methodName = method.getName();
            String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            fields.add(fieldName);
        }
        return new ArrayList<String>(fields);
    }

    @Override
    public <U, T extends JORMEntity<U>> String toColumnName(Class<T> entityType, String fieldName) {
        return fieldName;
    }
}
