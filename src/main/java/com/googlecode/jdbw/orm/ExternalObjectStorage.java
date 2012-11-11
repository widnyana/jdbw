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

import java.util.List;

public interface ExternalObjectStorage extends ObjectStorage {
    
    public static enum CachePolicy {
        LOCAL_GET,
        EXTERNAL_GET
    }
        
    <U, T extends Identifiable<U>> T get(Class<T> type, U key, CachePolicy searchPolicy);
    
    <U, T extends Identifiable<U>> List<T> getAll(Class<T> type, CachePolicy searchPolicy);
    
    void refresh();

    <U, T extends Identifiable<U>> void refresh(T... objects);

    <U, T extends Identifiable<U>> void refresh(Class<T> entityType);
    
    <U, T extends Identifiable<U>> void refresh(Class<T> objectType, U... keys);
}
