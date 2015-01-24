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

import com.googlecode.jdbw.objectstorage.ObjectCache;
import com.googlecode.jdbw.objectstorage.ObjectCacheFactory;
import com.googlecode.jdbw.objectstorage.Storable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapObjectCache<K, V extends Storable<K>> implements ObjectCache<K, V> {

    private final ConcurrentHashMap<K, V> hashMap;

    public ConcurrentHashMapObjectCache() {
        hashMap = new ConcurrentHashMap<K, V>();
    }
    
    @Override
    public V get(K key) {
        return hashMap.get(key);
    }

    @Override
    public Collection<V> values() {
        return hashMap.values();
    }

    @Override
    public void remove(Collection<K> ids) {
        hashMap.keySet().removeAll(ids);
    }

    @Override
    public void removeAll() {
        hashMap.clear();
    }

    @Override
    public void put(V o) {
        hashMap.put(o.getId(), o);
    }

    @Override
    public int size() {
        return hashMap.size();
    }
    
    public static class Factory implements ObjectCacheFactory {
        @Override
        public ObjectCache createObjectCache() {
            return new ConcurrentHashMapObjectCache();
        }        
    }
}
