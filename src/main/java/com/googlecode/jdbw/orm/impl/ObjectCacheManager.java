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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectCacheManager {
    private final Map<Class<? extends Identifiable>, DataCache> objectCache;

    public ObjectCacheManager() {
        this.objectCache = new HashMap<Class<? extends Identifiable>, DataCache>();
    }
    
    public <U, T extends Identifiable<U>> void createDataCache(Class<T> objectType) {
        synchronized(objectCache) {
            objectCache.put(objectType, new ConcurrentDataCache());
        }
    }
    
    public List<Class> getAllKnownObjectTypes() {
        synchronized(objectCache) {
            return Collections.unmodifiableList(new ArrayList(objectCache.keySet()));
        }
    }
    
    public <U, T extends Identifiable<U>> DataCache<U, T> getCache(Class<T> objectType) {
        synchronized(objectCache) {
            if(!objectCache.containsKey(objectType))
                throw new IllegalArgumentException("Trying to access unregistered object type " + objectType.getName());
            return objectCache.get(objectType);
        }
    }    
}
