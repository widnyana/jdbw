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
    
    public <O extends Storable> void register(Class<O> objectType) {
        if(objectType == null) {
            throw new IllegalArgumentException("Passing null to register(...) is not allowed");
        }
        storageCells.putIfAbsent(objectType, 
                new Cell(fieldMappingFactory.createFieldMapping(objectType), objectCacheFactory.createObjectCache()));
    }

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

    public <O extends Storable> List<O> getAll(Class<O> type) {
        if(type == null) {
            throw new IllegalArgumentException("Passing null type to getAll(...) is not allowed");
        }
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call getAll(...) on unregistered type " + type.getName());
        }
        return new ArrayList<O>((Collection)storageCells.get(type).values());
    }

    public <O extends Storable> int getSize(Class<O> type) {
        if(type == null) {
            throw new IllegalArgumentException("Passing null type to getSize(...) is not allowed");
        }
        if(!storageCells.containsKey(type)) {
            throw new IllegalArgumentException("Trying to call getSize(...) on unregistered type " + type.getName());
        }
        return storageCells.get(type).size();
    }

    public ObjectBuilderFactory getBuilderFactory() {
        return new DefaultObjectBuilderFactory() {
            @Override
            protected FieldMapping getFieldMapping(Class<? extends Storable> objectType) {
                if(storageCells.contains(objectType)) {
                    return storageCells.get(objectType).getFieldMapping();
                }
                else {
                    return super.getFieldMapping(objectType);
                }
            }
        };
    }

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
        if(storageCells.contains(candidate)) {
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
    
    private static class Cell implements ObjectCache {
        final FieldMapping fieldMapping;
        final ObjectCache cache;

        public Cell(FieldMapping fieldMapping, ObjectCache cache) {
            this.fieldMapping = fieldMapping;
            this.cache = cache;
        }

        public FieldMapping getFieldMapping() {
            return fieldMapping;
        }
        
        public Storable get(Object key) {
            return cache.get(key);
        }

        public Collection values() {
            return cache.values();
        }

        public void remove(Collection ids) {
            cache.remove(ids);
        }

        public void removeAll() {
            cache.removeAll();
        }

        public void put(Storable o) {
            cache.put(o);
        }
        
        public int size() {
            return cache.size();
        }
    }
}

