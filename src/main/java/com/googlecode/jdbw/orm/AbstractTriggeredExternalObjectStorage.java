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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTriggeredExternalObjectStorage extends AbstractExternalObjectStorage implements TriggerableObjectStore {
    
    private final Set<GlobalTrigger> globalTriggers;
    private final Map<Class, Set<ObjectTrigger>> classTypeTriggers;

    public AbstractTriggeredExternalObjectStorage() {
        this(CachePolicy.EXTERNAL_GET);
    }

    public AbstractTriggeredExternalObjectStorage(CachePolicy defaultCachePolicy) {
        super(defaultCachePolicy);
        globalTriggers = new HashSet<GlobalTrigger>();
        classTypeTriggers = new HashMap<Class, Set<ObjectTrigger>>();
    }

    @Override
    public void registerGlobalTrigger(GlobalTrigger trigger) {
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to registerGlobalTrigger(...) with null trigger");
        }
        synchronized(globalTriggers) {
            globalTriggers.add(trigger);
        }
    }

    @Override
    public <T extends Identifiable> void registerObjectTrigger(Class<T> objectType, ObjectTrigger<T> trigger) {
        if(objectType == null) {
            throw new IllegalArgumentException("Illegal call to registerTrigger(...) with null objectType");
        }
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to registerTrigger(...) with null trigger");
        }
        synchronized(classTypeTriggers) {
            if(!classTypeTriggers.containsKey(objectType)) {
                classTypeTriggers.put(objectType, new HashSet<ObjectTrigger>());
            }
            classTypeTriggers.get(objectType).add(trigger);
        }
    }

    @Override
    public void removeGlobalTrigger(GlobalTrigger trigger) {
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to removeGlobalTrigger(...) with null trigger");
        }
        synchronized(globalTriggers) {
            globalTriggers.remove(trigger);
        }
    }

    @Override
    public <T extends Identifiable> void removeObjectTrigger(Class<T> objectType, ObjectTrigger<T> trigger) {
        if(objectType == null) {
            throw new IllegalArgumentException("Illegal call to removeTrigger(...) with null objectType");
        }
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to removeTrigger(...) with null trigger");
        }
        synchronized(classTypeTriggers) {
            if(!classTypeTriggers.containsKey(objectType)) {
                return;
            }
            classTypeTriggers.get(objectType).remove(trigger);
        }
    }
    
    protected <U, T extends Identifiable<U>> void fireGlobalTriggersBeforeCreate(Class<T> objectType, U key, Map<String, Object> initialValues) {
        synchronized(globalTriggers) {
            for(GlobalTrigger globalTrigger: globalTriggers) {
                globalTrigger.onCreated(this, objectType, key, initialValues);
            }
        }
    }
}
