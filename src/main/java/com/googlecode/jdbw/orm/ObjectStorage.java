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
package com.googlecode.jdbw.orm;

import java.util.Collection;
import java.util.List;

public interface ObjectStorage {

    <U, T extends Identifiable<U>> T get(Class<T> type, U key);

    <U, T extends Identifiable<U>> List<T> getAll(Class<T> type);

    <U, V extends ObjectBuilder, T extends Identifiable<U> & Modifiable<V>> List<V> newObjects(final Class<T> type, U... ids);
    
    <U, V extends ObjectBuilder, T extends Identifiable<U> & Modifiable<V>> List<V> newObjects(final Class<T> type, Collection<U> ids);

    <U, V extends ObjectBuilder, T extends Identifiable<U> & Modifiable<V>> V newObject(Class<T> type, U id);

    <U, T extends Identifiable<U> & Modifiable> T persist(Persistable<U, T> persistable);

    <U, T extends Identifiable<U> & Modifiable> List<T> persist(Persistable<U, T>... persistables);

    <U, T extends Identifiable<U> & Modifiable> List<T> persist(Collection<Persistable<U, T>> persistables);

    <T extends Identifiable> void register(Class<T> objectType);
    
    <U, T extends Identifiable<U>> void delete(T... objects);

    <U, T extends Identifiable<U>> void delete(Collection<T> objects);
    
    <U, T extends Identifiable<U>> void delete(Class<T> objectType, U... ids);
    
    <U, T extends Identifiable<U>> void delete(Class<T> objectType, Collection<U> ids);    
}
