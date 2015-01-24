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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractObjectStorage implements ObjectStorage {

    @Override
    public <K, O extends Storable<K>> O get(Class<O> type, K id) {
        List<O> result = getSome(type, id);
        if(result.isEmpty()) {
            return null;
        }
        else {
            return result.get(0);
        }
    }

    @Override
    public <K, O extends Storable<K>> boolean contains(Class<O> type, K id) {
        return get(type, id) != null;
    }
    
    @Override
    public <K, O extends Storable<K>> List<O> getSome(Class<O> type, K... keys) {
        return getSome(type, Arrays.asList(keys));
    }

    @Override
    public <O extends Storable> List<O> putAll(O... objects) {
        return putAll(Arrays.asList(objects));
    }

    @Override
    public <O extends Storable> List<O> putAll(Collection<O> objects) {
        if(objects == null) {
            throw new IllegalArgumentException("Passing null to putAll(...) is not allowed");
        }
        List<O> toReturn = new ArrayList<O>();
        for(O object: objects) {
            if(object != null) {
                toReturn.add(put(object));
            }
        }
        return toReturn;
    }

    @Override
    public <O extends Storable> void remove(O... objects) {
        remove(Arrays.asList(objects));
    }

    @Override
    public <O extends Storable> void remove(Collection<O> objects) {
        Class<O> type = null;
        List<Object> keysToRemove = new ArrayList<Object>();
        for(O object: objects) {
            if(object != null) {
                if(type == null) {
                    type = getStorableTypeFromObject(object);
                    if(type == null) {
                        throw new ObjectStorageException("Couldn't derive the object type from " + object + " of type " + object.getClass());
                    }
                }
                keysToRemove.add(object.getId());
            }
        }
        if(type != null) {
            remove(type, keysToRemove);
        }
    }

    @Override
    public <K, O extends Storable<K>> void remove(Class<O> objectType, K... ids) {
        remove(objectType, Arrays.asList(ids));
    }

    protected <O extends Storable> Class<O> getStorableTypeFromObject(O object) throws ObjectStorageException {
        if(object instanceof SelfDescribingStorable) {
            return ((SelfDescribingStorable)object).storableType();
        }
        return null;
    }
}
