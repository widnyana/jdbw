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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

public interface ObjectStorage {
    
    public static enum SearchPolicy {
        LOCAL_ONLY,
        CHECK_DATABASE_IF_MISSING,
        REFRESH_FIRST
    }

    <U, T extends Identifiable<U>> T get(Class<T> type, U key);

    <U, T extends Identifiable<U>> T get(Class<T> type, U key, SearchPolicy searchPolicy);

    <U, T extends Identifiable<U>> ArrayList<T> getAll(Class<T> type);

    <U, T extends Identifiable<U>> List<T> newEntities(final Class<T> type, int numberOfEntities) throws SQLException;

    <U, T extends Identifiable<U>> List<T> newEntities(final Class<T> type, U... ids) throws SQLException;

    <U, T extends Identifiable<U>> T newEntity(Class<T> type) throws SQLException;

    <U, T extends Identifiable<U>> T newEntity(Class<T> type, U id) throws SQLException;

    <U, T extends Identifiable<U>> T persist(Persistable<T> persistable) throws SQLException;

    <U, T extends Identifiable<U>> List<T> persist(Persistable<T>... persistables) throws SQLException;

    <U, T extends Identifiable<U>> List<T> persist(Collection<Persistable<T>> persistables) throws SQLException;

    void refresh();

    void refresh(Executor executor);

    <U, T extends Identifiable<U>> void refresh(T... entities);

    <U, T extends Identifiable<U>> void refresh(Class<T> entityType);

    <U, T extends Identifiable<U>> void refresh(Class<T> entityType, U... keys);

    <U, T extends Identifiable<U>> void register(Class<T> entityType) throws SQLException;

    <U, T extends Identifiable<U>> void register(Class<T> entityType, ClassTableMapping classTableMapping) throws SQLException;

    <U, T extends Identifiable<U>> void register(Class<T> entityType, ClassTableMapping classTableMapping, EntityInitializer initializer) throws SQLException;

    <U, T extends Identifiable<U>> void remove(T... entities) throws SQLException;

    <U, T extends Identifiable<U>> void remove(Collection<T> entities) throws SQLException;

    <U, T extends Identifiable<U>> void remove(Class<T> entityType, U... ids) throws SQLException;

    <U, T extends Identifiable<U>> void remove(Class<T> entityType, Collection<U> ids) throws SQLException;
    
}
