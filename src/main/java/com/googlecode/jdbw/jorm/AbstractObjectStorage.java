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
package com.googlecode.jdbw.jorm;

import com.googlecode.jdbw.util.SelfExecutor;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractObjectStorage implements ObjectStorage {
    
    
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
    public <U, T extends Identifiable<U>> void remove(T... entities) throws SQLException {
        remove(Arrays.asList(entities));
    }
    
    @Override
    public <U, T extends Identifiable<U>> void remove(Class<T> entityType, U... ids) throws SQLException {
        remove(entityType, Arrays.asList(ids));
    }
        
    @Override
    public void refresh() {
        refresh(new SelfExecutor());
    }
}
