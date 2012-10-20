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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

public interface ObjectStorage {
    
    public static enum CachePolicy {
        SHALLOW_GET,
        SHALLOW_AND_DEEP_GET,
        DEEP_GET
    }

    <U, T extends Identifiable<U>> T get(Class<T> type, U key);
    
    <U, T extends Identifiable<U>> T get(Class<T> type, U key, CachePolicy searchPolicy);

    <U, T extends Identifiable<U>> ArrayList<T> getAll(Class<T> type);

    <U, T extends Identifiable<U>> List<T> newObjects(final Class<T> type, int numberOfObjects) throws SQLException;

    <U, T extends Identifiable<U>> List<T> newObjects(final Class<T> type, U... ids) throws SQLException;

    <U, T extends Identifiable<U>> T newObject(Class<T> type) throws SQLException;

    <U, T extends Identifiable<U>> T newObject(Class<T> type, U id) throws SQLException;

    <U, T extends Identifiable<U>> T persist(Persistable<T> persistable) throws SQLException;

    <U, T extends Identifiable<U>> List<T> persist(Persistable<T>... persistables) throws SQLException;

    <U, T extends Identifiable<U>> List<T> persist(Collection<Persistable<T>> persistables) throws SQLException;

    void refresh();

    void refresh(Executor executor);

    <U, T extends Identifiable<U>> void refresh(T... objects);

    <U, T extends Identifiable<U>> void refresh(Class<T> objectType, U... keys);

    <U, T extends Identifiable<U>> void register(Class<T> objectType) throws SQLException;
    
    <U, T extends Identifiable<U>> void register(Class<T> objectType, ObjectInitializer initializer) throws SQLException;

    <U, T extends Identifiable<U>> void delete(T... objects) throws SQLException;

    <U, T extends Identifiable<U>> void delete(Collection<T> objects) throws SQLException;
    
    <U, T extends Identifiable<U>> void delete(Class<T> objectType, U... ids) throws SQLException;
    
    <U, T extends Identifiable<U>> void delete(Class<T> objectType, Collection<U> ids) throws SQLException;
    
    <U, T extends Identifiable<U>> void registerTrigger(Class<T> objectType, Trigger trigger);
    
    <U, T extends Identifiable<U>> void removeTrigger(Class<T> objectType, Trigger trigger);
    
    void registerGlobalTrigger(Trigger trigger);
    
    void removeGlobalTrigger(Trigger trigger);    
}
