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

import com.googlecode.jdbw.objectstorage.FieldMapping;
import com.googlecode.jdbw.objectstorage.ObjectBuilder;
import com.googlecode.jdbw.objectstorage.ObjectBuilderFactory;
import com.googlecode.jdbw.objectstorage.ObjectFactory;
import com.googlecode.jdbw.objectstorage.ObjectStorageException;
import com.googlecode.jdbw.objectstorage.Storable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultObjectBuilderFactory implements ObjectBuilderFactory {
    
    private final ObjectFactory objectFactory;

    public DefaultObjectBuilderFactory() {
        this(new ImmutableObjectFactory());
    }
    
    public DefaultObjectBuilderFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
    
    @Override
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key) {
        return newObjects(builderType, key).get(0);
    }

    @Override
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key, O template) {
        if(builderType == null) {
            throw new IllegalArgumentException("Passing null type to DefaultObjectBuilderFactory.newObject(...) is not allowed");
        }
        if(template == null) {
            throw new IllegalArgumentException("Passing null template to DefaultObjectBuilderFactory.newObject(...) is not allowed");
        }
        return newBuilderProxy(builderType, key, template);
    }

    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key, Map<String, Object> initialValues) {
        if(builderType == null) {
            throw new IllegalArgumentException("Passing null type to DefaultObjectBuilderFactory.newObject(...) is not allowed");
        }
        if(initialValues == null) {
            throw new IllegalArgumentException("Passing null initialValues to DefaultObjectBuilderFactory.newObject(...) is not allowed");
        }
        return newBuilderProxy(builderType, key, initialValues);
    }

    @Override
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> List<B> newObjects(Class<B> builderType, K... keys) {
        return newObjects(builderType, Arrays.asList(keys));
    }

    @Override
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> List<B> newObjects(Class<B> builderType, Collection<K> keys) {
        if(builderType == null) {
            throw new IllegalArgumentException("Passing null type to ObjectBuilder.Factory.newObjects(...) is not allowed");
        }
        List<B> builders = new ArrayList<B>();
        for(K key: keys) {
            builders.add(newBuilderProxy(builderType, key));
        }
        return builders;
    }
        
    protected FieldMapping getFieldMapping(Class<? extends Storable> objectType) {
        return new DefaultFieldMapping(objectType);
    }
    
    private Class<? extends Storable> resolveStorable(Class builderType) {
        if(!ObjectBuilder.class.isAssignableFrom(builderType)) {
            return null;
        }
        for(Type type: builderType.getGenericInterfaces()) {
            if(type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if(parameterizedType.getRawType() == ObjectBuilder.class) {
                    if(Storable.class.isAssignableFrom((Class)parameterizedType.getActualTypeArguments()[0])) {
                        return (Class)parameterizedType.getActualTypeArguments()[0];
                    }
                }                
            }
        }
        throw new ObjectStorageException("Unknown storable type for builder " + builderType.getSimpleName());
    }

    private <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newBuilderProxy(Class<B> builderType, K key) throws IllegalArgumentException {
        return (B)Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(), 
                    new Class[] { builderType }, 
                    new BuilderProxyHandler(getFieldMapping(resolveStorable(builderType)), objectFactory, key));
    }

    private <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newBuilderProxy(Class<B> builderType, K key, O template) throws IllegalArgumentException {
        return (B)Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(), 
                    new Class[] { builderType }, 
                    new BuilderProxyHandler(getFieldMapping(resolveStorable(builderType)), objectFactory, key, template));
    }

    private <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newBuilderProxy(Class<B> builderType, K key, Map<String, Object> initialValues) throws IllegalArgumentException {
        return (B)Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(), 
                    new Class[] { builderType }, 
                    new BuilderProxyHandler(getFieldMapping(resolveStorable(builderType)), objectFactory, key, initialValues));
    }
}
