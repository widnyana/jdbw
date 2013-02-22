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

import com.googlecode.jdbw.objectstorage.AbstractExternalObjectStorage;
import com.googlecode.jdbw.objectstorage.ObjectStorage;
import com.googlecode.jdbw.objectstorage.Storable;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author mberglun
 */
public class CachedRemoteObjectStorage extends AbstractExternalObjectStorage {
    
    private final ObjectStorage remoteObjectStorage;

    public CachedRemoteObjectStorage(ObjectStorage remoteObjectStorage, ObjectStorage localCache) {
        super(localCache);
        this.remoteObjectStorage = remoteObjectStorage;
    }

    @Override
    public <O extends Storable> void register(Class<O> objectType) {
        remoteObjectStorage.register(objectType);
        getLocalStorage().register(objectType);
    }

    @Override
    public <O extends Storable> O put(O object) {
        try {
            return remoteObjectStorage.put(object);
        }
        finally {
            getLocalStorage().put(object);
        }
    }

    @Override
    public <O extends Storable> List<O> putAll(Collection<O> objects) {
        try {
            return remoteObjectStorage.putAll(objects);
        }
        finally {
            getLocalStorage().putAll(objects);
        }
    }
    
    @Override
    public <K, O extends Storable<K>> void remove(Class<O> objectType, Collection<K> ids) {
        remoteObjectStorage.remove(objectType, ids);
        localRemove(objectType, ids);
    }

    @Override
    public <O extends Storable> void removeAll(Class<O> objectType) {
        remoteObjectStorage.removeAll(objectType);
        localRemoveAll(objectType);
    }

    @Override
    public <K, O extends Storable<K>> List<O> remoteGetSome(Class<O> type, Collection<K> keys) {
        List<O> objects = remoteObjectStorage.getSome(type, keys);
        localPut(objects);
        return objects;
    }

    @Override
    public <O extends Storable> List<O> remoteGetAll(Class<O> type) {
        List<O> objects = remoteObjectStorage.getAll(type);
        localRemoveAll(type);
        localPut(objects);
        return objects;
    }

    @Override
    public <O extends Storable> int remoteGetSize(Class<O> type) {
        return remoteObjectStorage.getSize(type);
    }
    
}
