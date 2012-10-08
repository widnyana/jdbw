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

import com.googlecode.jdbw.util.SelfExecutor;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractObjectStorage implements ObjectStorage {
    
    private final Set<Trigger> globalTriggers;
    private final Map<Class, Set<Trigger>> classTypeTriggers;

    public AbstractObjectStorage() {
        globalTriggers = new HashSet<Trigger>();
        classTypeTriggers = new HashMap<Class, Set<Trigger>>();
    }    
    
    @Override
    public <U, T extends Identifiable<U>> void register(Class<T> entityType) throws SQLException {
        register(entityType, null);
    }
    
    @Override
    public <U, T extends Identifiable<U>> void register(Class<T> entityType, ClassTableMapping classTableMapping) throws SQLException {
        register(entityType, classTableMapping, null);
    }
    
    @Override
    public <U, T extends Identifiable<U>> T get(Class<T> type, U key) {
        return get(type, key, SearchPolicy.REFRESH_FIRST);
    }
    
    @Override
    public <U, T extends Identifiable<U>> T newEntity(Class<T> type) throws SQLException {
        return newEntity(type, (U)null);
    }
    
    @Override
    public <U, T extends Identifiable<U>> T persist(Persistable<T> persistable) throws SQLException {
        return persist(Arrays.asList(persistable)).get(0);
    }
    
    @Override
    public <U, T extends Identifiable<U>> List<T> persist(Persistable<T>... persistables) throws SQLException {
        return persist(Arrays.asList(persistables));
    }
    
    @Override
    public <U, T extends Identifiable<U>> void delete(T... entities) throws SQLException {
        delete(Arrays.asList(entities));
    }
    
    @Override
    public <U, T extends Identifiable<U>> void delete(Class<T> entityType, U... ids) throws SQLException {
        delete(entityType, Arrays.asList(ids));
    }
        
    @Override
    public void refresh() {
        refresh(new SelfExecutor());
    }

    @Override
    public void registerGlobalTrigger(Trigger trigger) {
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to registerGlobalTrigger(...) with null trigger");
        }
        synchronized(globalTriggers) {
            globalTriggers.add(trigger);
        }
    }

    @Override
    public <U, T extends Identifiable<U>> void registerTrigger(Class<T> entityType, Trigger trigger) {
        if(entityType == null) {
            throw new IllegalArgumentException("Illegal call to registerTrigger(...) with null entityType");
        }
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to registerTrigger(...) with null trigger");
        }
        synchronized(classTypeTriggers) {
            if(!classTypeTriggers.containsKey(entityType)) {
                classTypeTriggers.put(entityType, new HashSet<Trigger>());
            }
            classTypeTriggers.get(entityType).add(trigger);
        }
    }

    @Override
    public void removeGlobalTrigger(Trigger trigger) {
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to removeGlobalTrigger(...) with null trigger");
        }
        synchronized(globalTriggers) {
            globalTriggers.remove(trigger);
        }
    }

    @Override
    public <U, T extends Identifiable<U>> void removeTrigger(Class<T> entityType, Trigger trigger) {
        if(entityType == null) {
            throw new IllegalArgumentException("Illegal call to removeTrigger(...) with null entityType");
        }
        if(trigger == null) {
            throw new IllegalArgumentException("Illegal call to removeTrigger(...) with null trigger");
        }
        synchronized(classTypeTriggers) {
            if(!classTypeTriggers.containsKey(entityType)) {
                return;
            }
            classTypeTriggers.get(entityType).remove(trigger);
        }
    }
    
    protected <U, T extends Identifiable<U>> Set<Trigger> getTriggersForClass(Class<T> entityType) {
        if(entityType == null) {
            throw new IllegalArgumentException("Illegal call to getTriggersForClass(...) with null entityType");
        }
        Set<Trigger> triggers = new HashSet<Trigger>();
        synchronized(globalTriggers) {
            triggers.addAll(globalTriggers);
        }
        synchronized(classTypeTriggers) {
            if(classTypeTriggers.containsKey(entityType)) {
                triggers.addAll(classTypeTriggers.get(entityType));
            }
        }
        return Collections.unmodifiableSet(triggers);
    }
}
