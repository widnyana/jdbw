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
import java.lang.reflect.InvocationHandler;

abstract class CommonProxyHandler<U, T extends Identifiable<U>> implements InvocationHandler {
    abstract Class<T> getObjectType();
    abstract U getKey();

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getObjectType().isAssignableFrom(obj.getClass())) {
            return false;
        }
        return getKey().equals(((T) obj).getId());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public String toString() {
        return getKey().toString() + ":" + getObjectType().getSimpleName();
    }
}
