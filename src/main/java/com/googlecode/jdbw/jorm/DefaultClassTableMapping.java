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

public class DefaultClassTableMapping<U extends Comparable<U>, T extends JORMEntity<U>> implements ClassTableMapping {
    
    private final Class<T> entityType;

    public DefaultClassTableMapping(Class<T> entityType) {
        this.entityType = entityType;
    }

    public String getTableName() {
        return entityType.getSimpleName();
    }

    public String[] getNonIdColumns() {
        List<String> columns = new ArrayList<String>();
        for(Method method: entityType.getMethods()) {
            if((method.getModifiers() & Modifier.STATIC) != 0)
                continue;
            if(!method.getName().startsWith("get") || method.getName().length() <= 3)
                continue;
            if(method.getName().equals("getId"))
                continue;
            
            String methodName = method.getName();
            String columnName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            columns.add(columnName);
        }
        return columns.toArray(new String[0]);
    }    
}
