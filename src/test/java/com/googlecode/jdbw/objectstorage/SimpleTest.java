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

import com.googlecode.jdbw.objectstorage.impl.DefaultObjectBuilderFactory;
import com.googlecode.jdbw.objectstorage.impl.DefaultObjectStorage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleTest extends TestBase {
            
    @Test
    public void creatingDefaultObjectStorageWorks() {
        new DefaultObjectStorage();
    }
    
    @Test
    public void registeringTypeOnTheObjectStorageWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
    }
    
    @Test
    public void creatingNewPersonWithoutStorageWorks() {
        Person elvis = createElvis(new DefaultObjectBuilderFactory());
        assertEquals(1, elvis.getId().intValue());
        assertEquals(42, elvis.getAge());
        assertEquals("Elvis Presley", elvis.getName());
        assertEquals(ELVIS_BIRTHDAY, elvis.getBirthday());
    }
    
    @Test
    public void creatingNewPersonWithStorageWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        Person elvis = createElvis(objectStorage.getBuilderFactory());
        assertEquals(1, elvis.getId().intValue());
        assertEquals(42, elvis.getAge());
        assertEquals("Elvis Presley", elvis.getName());
        assertEquals(ELVIS_BIRTHDAY, elvis.getBirthday());
    }
    
    @Test
    public void creatingNewPersonBasedOnTemplateWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person clone = builderFactory.newObject(Person.Builder.class, 10, elvis).build();
        assertEquals(10, clone.getId().intValue());
        assertEquals(elvis.getAge(), clone.getAge());
        assertEquals(elvis.getBirthday(), clone.getBirthday());
        assertEquals(elvis.getName(), clone.getName());
    }
    
    @Test
    public void creatingNewPersonAndInsertingIntoTheObjectStorageWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        Person elvis = createElvis(objectStorage.getBuilderFactory());
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.put(elvis);
        assertEquals(1, objectStorage.getSize(Person.class));
        
        Person storagePerson = objectStorage.get(Person.class, 1);
        assertEquals(elvis.getAge(), storagePerson.getAge());
        assertEquals(elvis.getBirthday(), storagePerson.getBirthday());
        assertEquals(elvis.getId(), storagePerson.getId());
        assertEquals(elvis.getName(), storagePerson.getName());
        assertEquals(elvis, storagePerson);
    }
    
    @Test
    public void puttingMultipleObjectsIntoTheStorageWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person jaques = createJaques(builderFactory);
        Person sakamoto = createSakamoto(builderFactory);
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.putAll(elvis, jaques, sakamoto);
        assertEquals(3, objectStorage.getSize(Person.class));        
        assertEquals(elvis, objectStorage.get(Person.class, 1));
        assertEquals(jaques, objectStorage.get(Person.class, 2));
        assertEquals(sakamoto, objectStorage.get(Person.class, 3));
    }
    
    @Test
    public void getAllWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person jaques = createJaques(builderFactory);
        Person sakamoto = createSakamoto(builderFactory);
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.putAll(elvis, jaques, sakamoto);
        assertEquals(3, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(elvis, jaques, sakamoto), objectStorage.getAll(Person.class));
    }
    
    @Test
    public void getSomeWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person jaques = createJaques(builderFactory);
        Person sakamoto = createSakamoto(builderFactory);
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.putAll(elvis, jaques, sakamoto);
        assertEquals(3, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(elvis, sakamoto), objectStorage.getSome(Person.class, 1, 3));
        assertSetEquals(Arrays.asList(jaques, sakamoto), objectStorage.getSome(Person.class, 2, 3));
        assertSetEquals(Arrays.asList(jaques, elvis), objectStorage.getSome(Person.class, 2, 1));
    }
    
    @Test
    public void removeWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person jaques = createJaques(builderFactory);
        Person sakamoto = createSakamoto(builderFactory);
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.putAll(elvis, jaques, sakamoto);
        assertEquals(3, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(elvis, jaques, sakamoto), objectStorage.getAll(Person.class));
        objectStorage.remove(elvis);
        assertEquals(2, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(jaques, sakamoto), objectStorage.getAll(Person.class));
        objectStorage.remove(Person.class, 2);
        assertEquals(1, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(sakamoto), objectStorage.getAll(Person.class));
    } 
    
    @Test
    public void removeAllWorks() {
        ObjectStorage objectStorage = new DefaultObjectStorage();
        objectStorage.register(Person.class);
        ObjectBuilderFactory builderFactory = objectStorage.getBuilderFactory();
        Person elvis = createElvis(builderFactory);
        Person jaques = createJaques(builderFactory);
        Person sakamoto = createSakamoto(builderFactory);
        assertEquals(0, objectStorage.getSize(Person.class));
        objectStorage.putAll(elvis, jaques, sakamoto);
        assertEquals(3, objectStorage.getSize(Person.class));
        assertSetEquals(Arrays.asList(elvis, jaques, sakamoto), objectStorage.getAll(Person.class));
        objectStorage.removeAll(Person.class);
        assertEquals(0, objectStorage.getSize(Person.class));
    }

    private void assertSetEquals(Collection a, Collection b) {
        assertEquals(new HashSet(a), new HashSet(b));
    }
}
