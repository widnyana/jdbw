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

class ImmutableObjectProxyHandler<U, T extends Identifiable<U>> extends CommonProxyHandler<U, T> {

    private final TableDataStorage<U, T> dataStorage;
    private final U key;

    ImmutableObjectProxyHandler(TableDataStorage<U, T> dataStorage, U key) {
        this.dataStorage = dataStorage;
        this.key = key;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if("getId".equals(method.getName())) {
            return getKey();
        }
        else if(method.getName().startsWith("get") || method.getName().startsWith("is")) {
            return dataStorage.getValue(key, method.getName());
        }
        else if(method.getName().equals("equals") && 
                method.getParameterTypes().length == 1 && 
                method.getParameterTypes()[0] == Object.class) {
            return testForEquals(args[0]);
        }
        else if(method.getName().startsWith("set")) {
            throw new UnsupportedOperationException("Illegal call to set method on immutable object");
        }
        else {
            throw new UnsupportedOperationException("JDBW ORM doesn't support calling " + method.getName() + " yet");
        }
    }

    @Override
    Class<T> getObjectType() {
        return dataStorage.getObjectType();
    }    

    @Override
    U getKey() {
        return key;
    }

    private boolean testForEquals(Object arg) {
        if(arg != null && dataStorage.getObjectType().isAssignableFrom(arg.getClass())) {
            return getKey().equals(((T)arg).getId());
        }
        else {
            return false;
        }
    }
}
