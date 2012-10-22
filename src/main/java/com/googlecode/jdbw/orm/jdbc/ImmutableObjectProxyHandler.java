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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    Class<T> getObjectType() {
        return dataStorage.getObjectType();
    }    

    @Override
    U getKey() {
        return key;
    }
}
