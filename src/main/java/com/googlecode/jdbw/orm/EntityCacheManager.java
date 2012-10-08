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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EntityCacheManager {
    private final Map<Class<? extends Identifiable>, DataCache> entityCache;

    EntityCacheManager() {
        this.entityCache = new HashMap<Class<? extends Identifiable>, DataCache>();
    }
    
    <U, T extends Identifiable<U>> void createDataCache(Class<T> entityType) {
        synchronized(entityCache) {
            entityCache.put(entityType, new ConcurrentDataCache());
        }
    }
    
    List<Class> getAllKnownEntityTypes() {
        synchronized(entityCache) {
            return Collections.unmodifiableList(new ArrayList(entityCache.keySet()));
        }
    }
    
    <U, T extends Identifiable<U>> DataCache<U, T> getCache(Class<T> entityType) {
        synchronized(entityCache) {
            if(!entityCache.containsKey(entityType))
                throw new IllegalArgumentException("Trying to access unregistered entity type " + entityType.getName());
            return entityCache.get(entityType);
        }
    }    
}
