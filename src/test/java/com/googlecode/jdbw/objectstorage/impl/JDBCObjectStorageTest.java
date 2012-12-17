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

import com.googlecode.jdbw.objectstorage.H2DatabaseTestBase;
import com.googlecode.jdbw.objectstorage.ObjectBuilderFactory;
import com.googlecode.jdbw.objectstorage.Storable;
import com.googlecode.jdbw.util.SQLWorker;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class JDBCObjectStorageTest extends H2DatabaseTestBase {
    
    private static interface Unregistered extends Storable<UUID> {        
    }
    
    @Before
    public void extraBuildUp() throws SQLException {
        SQLWorker worker = getWorker();
        worker.write("CREATE TABLE \"Unregistered\" ("
                + "\"id\" CHAR(36) PRIMARY KEY)");
    }
    
    @After
    public void extraTearDown() throws SQLException {
        SQLWorker worker = getWorker();
        worker.write("DROP TABLE \"Unregistered\"");
        
    }
    
    @Test
    public void notDoingAnythingWorks() {
        assertTrue(true);
    }
    
    /**
     * Test of register method, of class JDBCObjectStorage.
     */
    @Test
    public void testRegister() {
        System.out.println("register");
        JDBCObjectStorage instance = getObjectStorage();
        
        try {
            instance.getAll(Unregistered.class);
            fail("getAll(Unregistered.class) didn't fail before the Unregistered interface was registered!");
        }
        catch(RuntimeException e) {
        }        
        instance.register(Unregistered.class);
        instance.getAll(Unregistered.class);
    }

    /**
     * Test of getBuilderFactory method, of class JDBCObjectStorage.
     */
    @Test
    public void testGetBuilderFactory() {
        System.out.println("getBuilderFactory");
        JDBCObjectStorage instance = getObjectStorage();
        ObjectBuilderFactory result = instance.getBuilderFactory();
        assertNotNull(result);
    }

    @Test
    public void getAllPreexistingPersonsWorks() throws SQLException {
        List<Person> persons = getObjectStorage().getAll(Person.class);
        Collections.sort(persons, new PersonIdComparator());
        assertEquals(3, persons.size());
        assertEquals((Integer) 1, persons.get(0).getId());
        assertEquals("Elvis Presley", persons.get(0).getName());
        assertEquals((Integer) 2, persons.get(1).getId());
        assertEquals("Jacques Brel", persons.get(1).getName());
        assertEquals((Integer) 3, persons.get(2).getId());
        assertEquals("Kyu Sakamoto", persons.get(2).getName());
    }

    @Test
    public void getOnePreexistingPersonWorks() throws SQLException {
        Person elvis = getObjectStorage().get(Person.class, 1);
        assertEquals("Elvis Presley", elvis.getName());
    }

    @Test
    public void modifyDatabaseExternallyWorks() throws SQLException {
        JDBCObjectStorage instance = getObjectStorage();
        List<Person> persons = instance.getAll(Person.class);
        assertEquals(3, persons.size());
        getWorker().write("DELETE FROM \"Person\" WHERE \"id\" = 2");
        persons = instance.getAll(Person.class);
        assertEquals(2, persons.size());
        assertEquals(42, instance.get(Person.class, 1).getAge());
        getWorker().write("UPDATE \"Person\" SET \"age\" = 50 WHERE \"id\" = 1");
        assertEquals(50, instance.get(Person.class, 1).getAge());
    }

    /**
     * Test of getSome method, of class JDBCObjectStorage.
     */
    @Test
    public void testGetSome() {
        System.out.println("getSome");
        JDBCObjectStorage instance = getObjectStorage();
        Set<Integer> expectedKeys = new HashSet<Integer>();
        expectedKeys.add(1);
        expectedKeys.add(2);
        List<Person> result = instance.getSome(Person.class, 1, 2);
        assertEquals(expectedKeys.size(), result.size());
        for(Person person: result) {
            assertNotNull(person);
            expectedKeys.remove(person.getId());
        }
        assertEquals(0, expectedKeys.size());
    }

    /**
     * Test of getAll method, of class JDBCObjectStorage.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        JDBCObjectStorage instance = getObjectStorage();
        Set<Integer> expectedKeys = new HashSet<Integer>();
        expectedKeys.add(1);
        expectedKeys.add(2);
        expectedKeys.add(3);
        List<Person> result = instance.getAll(Person.class);
        assertEquals(expectedKeys.size(), result.size());
        for(Person person: result) {
            assertNotNull(person);
            expectedKeys.remove(person.getId());
        }
        assertEquals(0, expectedKeys.size());
    }

    /**
     * Test of getSize method, of class JDBCObjectStorage.
     */
    @Test
    public void testGetSize() {
        System.out.println("getSize");
        JDBCObjectStorage instance = getObjectStorage();
        int expResult = 3;
        int result = instance.getSize(Person.class);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class JDBCObjectStorage.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        JDBCObjectStorage instance = getObjectStorage();
        Person.Builder builder = instance.getBuilderFactory().newObject(Person.Builder.class, 4);
        builder.setAge(85);
        builder.setBirthday(new DateMidnight("1890-03-12").toDate());
        builder.setName("Evert Tabue");
        Person evert = builder.build();
        Person result = instance.put(evert);
        assertEquals(evert, result);
        result = instance.get(Person.class, 4);
        assertEquals(evert, result);
        assertEquals(4, (int)evert.getId());
        assertEquals(4, (int)result.getId());
        assertEquals(85, result.getAge());
        assertEquals("Evert Tabue", result.getName());
        assertEquals(new DateMidnight("1890-03-12").toDate(), result.getBirthday());
        assertEquals(4, instance.getSize(Person.class));
        
        builder = instance.getBuilderFactory().newObject(Person.Builder.class, 4, evert);
        builder.setName("Evert Taube");
        evert = builder.build();
        result = instance.put(evert);
        assertEquals(evert, result);
        result = instance.get(Person.class, 4);
        assertEquals(evert, result);
        assertEquals(4, (int)evert.getId());
        assertEquals(4, (int)result.getId());
        assertEquals(85, result.getAge());
        assertEquals("Evert Taube", result.getName());
        assertEquals(new DateMidnight("1890-03-12").toDate(), result.getBirthday());
        assertEquals(4, instance.getSize(Person.class));        
    }

    /**
     * Test of putAll method, of class JDBCObjectStorage.
     */
    @Test
    public void testPutAll() {
        System.out.println("putAll");
        JDBCObjectStorage instance = getObjectStorage();
        Set<Person> expectedResult = new HashSet<Person>();
        Person.Builder builder = instance.getBuilderFactory().newObject(Person.Builder.class, 4);
        builder.setAge(85);
        builder.setBirthday(new DateMidnight("1890-03-12").toDate());
        builder.setName("Evert Taube");
        expectedResult.add(builder.build());
        
        builder = instance.getBuilderFactory().newObject(Person.Builder.class, 5);
        builder.setAge(71);
        builder.setBirthday(new DateMidnight("1932-02-26").toDate());
        builder.setName("Johnny Cash");
        expectedResult.add(builder.build());
        
        instance.putAll(expectedResult);
        Set<Person> result = new HashSet<Person>(instance.getSome(Person.class, 4, 5));
        assertEquals(expectedResult, result);
    }

    /**
     * Test of remove method, of class JDBCObjectStorage.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        JDBCObjectStorage instance = getObjectStorage();
        Map<Integer, Person> allPersons = new HashMap<Integer, Person>();
        for(Person person: instance.getAll(Person.class)) {
            allPersons.put(person.getId(), person);
        }
        assertEquals(allPersons.size(), instance.getSize(Person.class));
        Person randomPerson = allPersons.values().iterator().next();
        instance.remove(randomPerson);
        allPersons.remove(randomPerson.getId());
        assertEquals(allPersons.size(), instance.getSize(Person.class));
        assertEquals(new HashSet<Person>(allPersons.values()), 
                new HashSet<Person>(instance.getAll(Person.class)));
    }

    /**
     * Test of removeAll method, of class JDBCObjectStorage.
     */
    @Test
    public void testRemoveAll() {
        System.out.println("removeAll");
        JDBCObjectStorage instance = getObjectStorage();
        assertTrue(instance.getSize(Person.class) > 0);
        instance.removeAll(Person.class);
        assertEquals(0, instance.getSize(Person.class));
    }
    
    private static class PersonIdComparator implements Comparator<Person> {
        @Override
        public int compare(Person o1, Person o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }
}
