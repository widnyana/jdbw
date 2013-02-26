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
import com.googlecode.jdbw.objectstorage.SelfDescribingStorable;
import com.googlecode.jdbw.objectstorage.Storable;
import java.lang.reflect.Proxy;

public class ImmutableObjectFactory implements ObjectFactory {
    @Override
    public <O extends Storable> O newObject(Class<O> type, FieldMapping fieldMapping, Object[] idAndValues) {
        Object[] valuesOnly = new Object[idAndValues.length - 1];
        System.arraycopy(idAndValues, 1, valuesOnly, 0, idAndValues.length - 1);
        return (O)Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(), 
                new Class[] { type, SelfDescribingStorable.class }, 
                new ObjectProxyHandler(fieldMapping, idAndValues[0], valuesOnly));
    }    
}
