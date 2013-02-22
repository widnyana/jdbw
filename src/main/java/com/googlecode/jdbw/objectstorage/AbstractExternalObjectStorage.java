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
package com.googlecode.jdbw.objectstorage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractExternalObjectStorage extends AbstractObjectStorage implements ExternalObjectStorage {
    private final ObjectStorage backend;

    public AbstractExternalObjectStorage(ObjectStorage backend) {
        this.backend = backend;
    }

    @Override
    public ObjectBuilderFactory getBuilderFactory() {
        return backend.getBuilderFactory();
    }

    @Override
    protected <O extends Storable> Class<O> getStorableTypeFromObject(O object) throws ObjectStorageException {
        if(backend instanceof AbstractObjectStorage) {
            return ((AbstractObjectStorage)backend).getStorableTypeFromObject(object);
        }
        else {
            throw new ObjectStorageException("Cannot figure out which storable type to use for " + 
                    object + " of class " + object.getClass() + ", please override getStorableTypeFromObject(...) in " +
                    getClass().getName());
        }
    }
    
    @Override
    public <K, O extends Storable<K>> O get(Class<O> type, K key) {
        O object = backend.get(type, key);
        if(object == null) {
            object = remoteGet(type, key);
        }
        return object;
    }

    @Override
    public <K, O extends Storable<K>> List<O> getSome(Class<O> type, Collection<K> keys) {
        List<O> objects = backend.getSome(type, keys);
        if(objects.isEmpty()) {
            objects = remoteGetSome(type, keys);
        }
        return objects;
    }

    @Override
    public <O extends Storable> List<O> getAll(Class<O> type) {
        List<O> objects = backend.getAll(type);
        if(objects.isEmpty()) {
            objects = remoteGetAll(type);
        }
        return objects;
    }

    @Override
    public <O extends Storable> int getSize(Class<O> type) {
        int size = backend.getSize(type);
        if(size == 0) {
            return remoteGetSize(type);
        }
        return size;
    }

    @Override
    public <O extends Storable> O put(O object) {
        return putAll(object).get(0);
    }
    
    protected <O extends Storable> List<O> localGetAll(Class<O> type) {
        return backend.getAll(type);
    }
    
    protected <O extends Storable> void localPut(Collection<O> objects) {
        backend.putAll(objects);
    }
    
    @Override
    public <O extends Storable> void localRemove(O... objects) {
        backend.remove(objects);
    }

    @Override
    public <O extends Storable> void localRemove(Collection<O> objects) {
        backend.remove(objects);
    }

    @Override
    public <K, O extends Storable<K>> void localRemove(Class<O> objectType, K... ids) {
        backend.remove(objectType, ids);
    }

    @Override
    public <K, O extends Storable<K>> void localRemove(Class<O> objectType, Collection<K> ids) {
        backend.remove(objectType, ids);
    }

    @Override
    public <K, O extends Storable<K>> void localRemoveAll(Class<O> objectType) {
        backend.removeAll(objectType);
    }

    @Override
    public <K, O extends Storable<K>> O remoteGet(Class<O> type, K key) {
        List<O> objects = remoteGetSome(type, Arrays.asList(key));
        if(!objects.isEmpty()) {
            return objects.get(1);
        }
        else {
            return null;
        }
    }

    @Override
    public <K, O extends Storable<K>> List<O> remoteGetSome(Class<O> type, K... keys) {
        return remoteGetSome(type, Arrays.asList(keys));
    }
}
