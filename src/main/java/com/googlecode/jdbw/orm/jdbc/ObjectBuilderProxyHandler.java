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
import java.util.Map;

class ObjectBuilderProxyHandler<U, T extends Identifiable<U> & Modifiable> extends AbstractBuilderProxyHandler<U, T> {

    public ObjectBuilderProxyHandler(FieldMapping fieldMapping, U key, Map<String, Object> initialValues) {
        super(fieldMapping, key, initialValues);
        if(key == null) {
            throw new IllegalArgumentException("Cannot create ModifiableObjectProxyHandler with null key");
        }
    }

    @Override
    protected void setValue(String fieldName, Object value) {
        if("id".equals(fieldName)) {
            throw new IllegalArgumentException("Cannot re-assign the id");
        }
        super.setValue(fieldName, value);
    }

    @Override
    public String toString() {
        return "Updatable{" + super.toString() + "}";
    }

    @Override
    protected Finalized makeFinalizedVersion() {
        return new Finalized(getObjectType(), getKey(), copyValuesToArray(false));
    }
    
    static class Finalized<U, T extends Identifiable<U> & Modifiable> extends AbstractBuilderProxyHandler.Finalized<U, T> {
        public Finalized(Class<T> objectType, U id, Object[] values) {
            super(objectType, id, values);
        }
    }    
}
