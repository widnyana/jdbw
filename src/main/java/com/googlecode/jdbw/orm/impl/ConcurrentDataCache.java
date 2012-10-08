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
    private final Map<U, T> entityCache;

    public ConcurrentDataCache() {
        entityCache = new ConcurrentHashMap<U, T>();
    }

    @Override
    public T get(U id) {
        return entityCache.get(id);
    }

    @Override
    public boolean contains(U id) {
        return entityCache.containsKey(id);
    }

    @Override
    public void put(T entity) {
        entityCache.put(entity.getId(), entity);
    }

    @Override
    public boolean remove(U id) {
        return entityCache.remove(id) != null;
    }

    @Override
    public boolean removeAll(Collection<U> ids) {
        return entityCache.keySet().removeAll(ids);
    }

    @Override
    public Set<U> allIds() {
        return Collections.unmodifiableSet(entityCache.keySet());
    }

    @Override
    public Collection<T> allValues() {
        return Collections.unmodifiableCollection(entityCache.values());
    }
}
