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
import com.googlecode.jdbw.orm.ObjectInitializer;
import java.util.HashMap;
import java.util.Map;

class ObjectInitializers {
   private final Map<Class, ObjectInitializer> initializers;

    ObjectInitializers() {
        initializers = new HashMap<Class, ObjectInitializer>();
    }
    
    <U, T extends Identifiable<U>> ObjectInitializer get(Class<T> objectType) {
        synchronized(initializers) {
            return initializers.get(objectType);
        }
    }
    
    <U, T extends Identifiable<U>> boolean add(Class<T> objectType, ObjectInitializer initializer) {
        synchronized(initializers) {
            if(!initializers.containsKey(objectType)) {
                initializers.put(objectType, initializer);
                return true;
            }
            else {
                return false;
            }
        }
    } 
}
