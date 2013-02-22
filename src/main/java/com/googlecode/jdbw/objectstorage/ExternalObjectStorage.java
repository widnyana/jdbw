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

import java.util.Collection;
import java.util.List;

public interface ExternalObjectStorage extends ObjectStorage {
    <K, O extends Storable<K>> O remoteGet(Class<O> type, K key);
    
    <K, O extends Storable<K>> O remoteGet(O object);
    
    <K, O extends Storable<K>> O localGet(Class<O> type, K key);
    
    <K, O extends Storable<K>> List<O> remoteGetSome(Class<O> type, K... keys);
    
    <K, O extends Storable<K>> List<O> remoteGetSome(Class<O> type, Collection<K> keys);

    <O extends Storable> List<O> remoteGetAll(Class<O> type);
    
    <O extends Storable> int remoteGetSize(Class<O> type);
    
    <O extends Storable> void localRemove(O... objects);

    <O extends Storable> void localRemove(Collection<O> objects);
    
    <K, O extends Storable<K>> void localRemove(Class<O> objectType, K... ids);
    
    <K, O extends Storable<K>> void localRemove(Class<O> objectType, Collection<K> ids);
    
    <K, O extends Storable<K>> void localRemoveAll(Class<O> objectType);
}
