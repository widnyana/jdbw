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

import com.googlecode.jdbw.objectstorage.AbstractObjectStorage;
import com.googlecode.jdbw.objectstorage.FieldMapping;
import com.googlecode.jdbw.objectstorage.FieldMappingFactory;
import com.googlecode.jdbw.objectstorage.ObjectBuilderFactory;
import com.googlecode.jdbw.objectstorage.ObjectCache;
import com.googlecode.jdbw.objectstorage.ObjectCacheFactory;
import com.googlecode.jdbw.objectstorage.ObjectStorageException;
import com.googlecode.jdbw.objectstorage.Storable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultObjectStorage extends AbstractObjectStorage {
    
    private final FieldMappingFactory fieldMappingFactory;
    private final ObjectCacheFactory objectCacheFactory;
    private final ConcurrentHashMap<Class, Cell> storageCells;

    public DefaultObjectStorage() {
        this(new DefaultFieldMappingFactory());
    }

    public DefaultObjectStorage(FieldMappingFactory fieldMappingFactory) {
        this(fieldMappingFactory, new ConcurrentHashMapObjectCache.Factory());
    }

    public DefaultObjectStorage(FieldMappingFactory fieldMappingFactory, ObjectCacheFactory objectCacheFactory) {
        this.fieldMappingFactory = fieldMappingFactory;
        this.objectCacheFactory = objectCacheFactory;
        storageCells = new ConcurrentHashMap<Class, Cell>();
    }
    
    @Override
    public <O extends Storable> void register(Class<O> objectType) {
        if(objectType == null) {
            throw new IllegalArgumentException("Passing null to register(...) is not allowed");
        }
        storageCells.putIfAbsent(objectType, 
                new Cell(fieldMappingFactory.createFieldMapping(objectType), objectCacheFactory.createObjectCache()));
    }

    @Override
    public <O extends Storable> boolean contains(O object) {
        if(object == null) {
            throw new IllegalArgumentException("Passing null object to contains(...) is not allowed");
        }
        Class objectType = object.getClass();
        if(object instanceof Proxy) {
            objectType = ((ObjectProxyHandler)Proxy.getInvocationHandler(object)).getObjectType();
        }
        return contains(objectType, object.getId());
    }

    @Override
    public <K, O extends Storable<K>> boolean contains(Class<O> type, K id) {
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call contains(...) on unregistered type " + type.getName());
        }
        return storageCells.get(type).get(id) != null;
    }

    @Override
    public <K, O extends Storable<K>> List<O> getSome(Class<O> type, Collection<K> keys) {
        if(type == null) {
            throw new IllegalArgumentException("Passing null type to getSome(...) is not allowed");
        }
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call getSome(...) on unregistered type " + type.getName());
        }
        List<O> toReturn = new ArrayList<O>();
        for(K key: keys) {
            O value = (O)storageCells.get(type).get(key);
            if(value != null) {
                toReturn.add(value);
            }
        }
        return toReturn;
    }

    @Override
    public <O extends Storable> List<O> getAll(Class<O> type) {
        if(type == null) {
            throw new IllegalArgumentException("Passing null type to getAll(...) is not allowed");
        }
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call getAll(...) on unregistered type " + type.getName());
        }
        return new ArrayList<O>(storageCells.get(type).values());
    }

    @Override
    public <O extends Storable> int getSize(Class<O> type) {
        if(type == null) {
            throw new IllegalArgumentException("Passing null type to getSize(...) is not allowed");
        }
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call getSize(...) on unregistered type " + type.getName());
        }
        return storageCells.get(type).size();
    }

    @Override
    public ObjectBuilderFactory getBuilderFactory() {
        return new DefaultObjectBuilderFactory() {
            @Override
            protected FieldMapping getFieldMapping(Class<? extends Storable> objectType) {
                if(storageCells.containsKey(objectType)) {
                    return storageCells.get(objectType).getFieldMapping();
                }
                else {
                    return super.getFieldMapping(objectType);
                }
            }
        };
    }

    @Override
    public <O extends Storable> O put(O object) {
        if(object == null) {
            throw new IllegalArgumentException("Passing null object to put(...) is not allowed");
        }
        Class objectType = object.getClass();
        if(object instanceof Proxy) {
            objectType = ((ObjectProxyHandler)Proxy.getInvocationHandler(object)).getObjectType();
        }
        if(!storageCells.containsKey(objectType)) {
            throw new IllegalArgumentException("Trying to call put(...) on unregistered type " + objectType.getName());
        }
        storageCells.get(objectType).put(object);
        return object;
    }

    @Override
    public <K, O extends Storable<K>> void remove(Class<O> objectType, Collection<K> ids) {
        if(objectType == null) {
            throw new IllegalArgumentException("Passing null object type to remove(...) is not allowed");
        }
        if(ids == null) {
            throw new IllegalArgumentException("Passing null ids to remove(...) is not allowed");
        }
        if(!storageCells.containsKey(objectType)) {
            throw new IllegalArgumentException("Trying to call remove(...) on unregistered type " + objectType.getName());
        }
        storageCells.get(objectType).remove(ids);
    }

    @Override
    public <O extends Storable> void removeAll(Class<O> objectType) {
        if(objectType == null) {
            throw new IllegalArgumentException("Passing null object type to removeAll(...) is not allowed");
        }
        if(!storageCells.containsKey(objectType)) {
            throw new IllegalArgumentException("Trying to call removeAll(...) on unregistered type " + objectType.getName());
        }
        storageCells.get(objectType).removeAll();
    }

    @Override
    protected <O extends Storable> Class<O> getStorableTypeFromObject(O object) throws ObjectStorageException {
        Class<O> type = super.getStorableTypeFromObject(object);
        if(type != null) {
            return type;
        }
        
        //Custom detection
        Class candidate = (Class)object.getClass();
        if(storageCells.containsKey(candidate)) {
           type = candidate; 
        }
        else if(object instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
            if(invocationHandler instanceof ObjectProxyHandler) {
                type = (Class)((ObjectProxyHandler)invocationHandler).getFieldMapping().getObjectType();
            }
        }        
        return type;
    }
    
    private static class Cell<K, V extends Storable<K>> implements ObjectCache<K, V> {
        final FieldMapping fieldMapping;
        final ObjectCache<K, V> cache;

        public Cell(FieldMapping fieldMapping, ObjectCache cache) {
            this.fieldMapping = fieldMapping;
            this.cache = cache;
        }

        public FieldMapping getFieldMapping() {
            return fieldMapping;
        }
        
        @Override
        public V get(K key) {
            return cache.get(key);
        }

        @Override
        public Collection<V> values() {
            return cache.values();
        }

        @Override
        public void remove(Collection<K> ids) {
            cache.remove(ids);
        }

        @Override
        public void removeAll() {
            cache.removeAll();
        }

        @Override
        public void put(V o) {
            cache.put(o);
        }
        
        @Override
        public int size() {
            return cache.size();
        }
    }
}

