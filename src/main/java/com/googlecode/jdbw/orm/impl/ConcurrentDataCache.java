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
package com.googlecode.jdbw.orm.impl;

import com.googlecode.jdbw.orm.Identifiable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentDataCache<U, T extends Identifiable<U>> implements DataCache<U,T> {
    private final Map<U, T> objectCache;

    public ConcurrentDataCache() {
        objectCache = new ConcurrentHashMap<U, T>();
    }

    @Override
    public T get(U id) {
        return objectCache.get(id);
    }

    @Override
    public boolean contains(U id) {
        return objectCache.containsKey(id);
    }

    @Override
    public void put(T object) {
        objectCache.put(object.getId(), object);
    }

    @Override
    public boolean remove(U id) {
        return objectCache.remove(id) != null;
    }

    @Override
    public boolean removeAll(Collection<U> ids) {
        return objectCache.keySet().removeAll(ids);
    }

    @Override
    public Set<U> allIds() {
        return Collections.unmodifiableSet(objectCache.keySet());
    }

    @Override
    public Collection<T> allValues() {
        return Collections.unmodifiableCollection(objectCache.values());
    }
}
