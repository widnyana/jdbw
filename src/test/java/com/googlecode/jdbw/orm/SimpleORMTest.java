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

import com.googlecode.jdbw.orm.Identifiable;
import com.googlecode.jdbw.orm.Persistable;
import com.googlecode.jdbw.orm.DefaultObjectInitializer;
import com.googlecode.jdbw.orm.Modifiable;
import com.googlecode.jdbw.orm.old.DatabaseObjectStorage;
import com.googlecode.jdbw.orm.old.DefaultClassTableMapping;
import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.server.h2.H2InMemoryServer;
import com.googlecode.jdbw.util.SQLWorker;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleORMTest {
    
    private static interface Person extends Identifiable<Integer>, Modifiable {
        String getName();
        Person setName(String name);
        int getAge();
        Person setAge(int age);
        Date getBirthday();
        Person setBirthday(Date birthday);
        
        @Override
        Person modify();
        
        @Override
        Persistable<Person> finish();
    }
    
    private final DatabaseConnection h2;

    public SimpleORMTest() {
        h2 = new H2InMemoryServer("junit").connect();
    }
    
    @Before
    public void buildUp() throws SQLException {
        SQLWorker worker = new SQLWorker(h2.createAutoExecutor());
        worker.write("CREATE TABLE \"Person\" ("
                + "\"id\" INT AUTO_INCREMENT PRIMARY KEY, "
                + "\"name\" VARCHAR, "
                + "\"age\" INT, "
                + "\"birthday\" DATE)");
        worker.write("INSERT INTO \"Person\" (\"name\", \"age\", \"birthday\") VALUES(?, ?, ?)", "Elvis Presley", 42, "1935-01-08");
        worker.write("INSERT INTO \"Person\" (\"name\", \"age\", \"birthday\") VALUES(?, ?, ?)", "Jacques Brel", 49, "1929-04-08");
        worker.write("INSERT INTO \"Person\" (\"name\", \"age\", \"birthday\") VALUES(?, ?, ?)", "Kyu Sakamoto", 43, "1941-11-10");
    }
    
    @After
    public void tearDown() throws SQLException {
        SQLWorker worker = new SQLWorker(h2.createAutoExecutor());
        worker.write("DROP TABLE \"Person\"");
        h2.close();
    }
    
    @Test
    public void notDoingAnythingWorks() {
        assertTrue(true);
    }
    
    @Test
    public void settingUpAndRegisteringJORMInterfaceWorks() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        jorm.refresh();
        List<Person> persons = jorm.getAll(Person.class);
        Collections.sort(persons, new PersonIdComparator());
        assertEquals(3, persons.size());
        assertEquals((Integer)1, persons.get(0).getId());
        assertEquals("Elvis Presley", persons.get(0).getName());
        assertEquals((Integer)2, persons.get(1).getId());
        assertEquals("Jacques Brel", persons.get(1).getName());
        assertEquals((Integer)3, persons.get(2).getId());
        assertEquals("Kyu Sakamoto", persons.get(2).getName());
    }
    
    @Test
    public void testingLocalAndRemoteSearch() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        assertNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.LOCAL_GET));
        assertNotNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.LOCAL_THEN_EXTERNAL_GET));
        assertNotNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.LOCAL_GET));
        assertEquals((Integer)1, jorm.get(Person.class, 1).getId());
    }
    
    @Test
    public void testingRefreshSearch() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        assertNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.LOCAL_GET));
        assertNotNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.EXTERNAL_GET));
        assertNotNull(jorm.get(Person.class, 1, DatabaseObjectStorage.CachePolicy.LOCAL_GET));
        assertEquals((Integer)1, jorm.get(Person.class, 1).getId());
    }
    
    @Test
    public void testingRefresh() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        Person kyoChan = jorm.get(Person.class, 3);
        assertNotNull(kyoChan);
        assertEquals(43, kyoChan.getAge());
        
        int ageThisYear = Calendar.getInstance().get(Calendar.YEAR) - 1941;
        new SQLWorker(h2.createAutoExecutor()).write("UPDATE \"Person\" SET \"age\" = ? WHERE \"id\" = ?", ageThisYear, 3);
        assertEquals(43, kyoChan.getAge());
        jorm.refresh(Person.class, 3);
        assertEquals(ageThisYear, kyoChan.getAge());
    }
    
    @Test
    public void canInsertNewRowsIntoTheJORM() throws ParseException, SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        Person reinhard = jorm.persist(
                            jorm.newObject(Person.class)
                                .setName("Reinhard Mey")
                                .setAge(69)
                                .setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1942-12-21"))
                                .finish());
        jorm.refresh();
        assertNotNull(jorm.get(Person.class, 4));
        assertEquals(reinhard, jorm.get(Person.class, 4));
        assertEquals("Elvis Presley", jorm.get(Person.class, 1).getName());
    }
    
    @Test
    public void canBatchInsertMultipleRows() throws ParseException, SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        List<Person> newPersons = jorm.persist(
                    jorm.newObject(Person.class)
                            .setName("Reinhard Mey")
                            .setAge(69)
                            .setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1942-12-21"))
                            .finish(),
                    jorm.newObject(Person.class)
                            .setName("Evert Taube")
                            .setAge(85)
                            .setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1890-03-12"))
                            .finish());
        jorm.refresh();
        assertNotNull(jorm.get(Person.class, 4));
        assertEquals(newPersons.get(0), jorm.get(Person.class, 4));
        assertNotNull(jorm.get(Person.class, 5));
        assertEquals(newPersons.get(1), jorm.get(Person.class, 5));
        assertEquals("Elvis Presley", jorm.get(Person.class, 1).getName());
    }
    
    @Test
    public void testingEquality() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        Person brel = jorm.get(Person.class, 2);
        assertEquals(brel, brel);
        assertTrue(brel.equals(brel));
        assertTrue(brel.equals(new Person() {
            @Override public Integer getId() {
                return 2;
            }
            
            @Override public String getName() {
                return "Jacques Brel";
            }
            
            @Override public int getAge() { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Date getBirthday() { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Person setName(String name) { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Person setAge(int age) { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Person setBirthday(Date birthday) { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Person modify() { throw new UnsupportedOperationException("Not supported yet."); }
            @Override public Persistable<Person> finish() { throw new UnsupportedOperationException("Not supported yet."); }
        }));
    }
    
    @Test
    public void testingEqualityAcrossJORMs() throws SQLException {
        DatabaseObjectStorage jorm1 = new DatabaseObjectStorage(h2);
        DatabaseObjectStorage jorm2 = new DatabaseObjectStorage(h2);
        jorm1.register(Person.class);
        jorm2.register(Person.class);
        Person brel1 = jorm1.get(Person.class, 2);
        Person brel2 = jorm1.get(Person.class, 2);
        assertEquals(brel1, brel2);
        assertEquals(brel2, brel1);
        assertTrue(brel1.equals(brel2));
        assertTrue(brel2.equals(brel1));
    }
    
    @Test
    public void removingOneEntityByInstanceWorks() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        jorm.refresh();
        Person elvis = jorm.get(Person.class, 1);
        assertNotNull(elvis);
        jorm.delete(elvis);
        assertNull(jorm.get(Person.class, 1));
        jorm.refresh();
        assertNull(jorm.get(Person.class, 1));
        assertEquals(2, jorm.getAll(Person.class).size());
    }
    
    @Test
    public void removingOneEntityByKeyWorks() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        jorm.refresh();
        Person elvis = jorm.get(Person.class, 1);
        assertNotNull(elvis);
        jorm.delete(Person.class, 1);
        assertNull(jorm.get(Person.class, 1));
        jorm.refresh();
        assertNull(jorm.get(Person.class, 1));
        assertEquals(2, jorm.getAll(Person.class).size());
    }
    
    @Test
    public void removingMultipleEntitiesByInstanceWorks() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        jorm.refresh();
        Person elvis = jorm.get(Person.class, 1);
        Person jaques = jorm.get(Person.class, 2);
        assertNotNull(elvis);
        assertNotNull(jaques);
        jorm.delete(elvis, jaques);
        assertNull(jorm.get(Person.class, 1));
        assertNull(jorm.get(Person.class, 2));
        jorm.refresh();
        assertNull(jorm.get(Person.class, 1));
        assertNull(jorm.get(Person.class, 2));
        assertEquals(1, jorm.getAll(Person.class).size());
    }
    
    @Test
    public void removingMultipleEntitiesByKeyWorks() throws SQLException {
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class);
        jorm.refresh();
        Person elvis = jorm.get(Person.class, 1);
        Person jaques = jorm.get(Person.class, 2);
        assertNotNull(elvis);
        assertNotNull(jaques);
        jorm.delete(Person.class, 1, 2);
        assertNull(jorm.get(Person.class, 1));
        assertNull(jorm.get(Person.class, 2));
        jorm.refresh();
        assertNull(jorm.get(Person.class, 1));
        assertNull(jorm.get(Person.class, 2));
        assertEquals(1, jorm.getAll(Person.class).size());
    }
    
    @Test
    public void usingEntityInitializerWorks() throws SQLException, ParseException { 
        DatabaseObjectStorage jorm = new DatabaseObjectStorage(h2);
        jorm.register(Person.class, new DefaultClassTableMapping(), new DefaultObjectInitializer() {
            @Override
            public <U, T extends Identifiable<U>> Object getInitialValue(Class<T> entityType, String fieldName) {
                if(entityType.equals(Person.class) && fieldName.equals("age")) {
                    return 17;
                }
                return super.getInitialValue(entityType, fieldName);
            }
        });
        Person reinhard = jorm.persist(
                            jorm.newObject(Person.class)
                                .setName("Reinhard Mey")
                                .setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1942-12-21"))
                                .finish());
        assertEquals(17, reinhard.getAge());
        assertEquals(17, jorm.get(Person.class, 4).getAge());
        jorm.refresh();
        assertEquals(17, reinhard.getAge());
        assertEquals(17, jorm.get(Person.class, 4).getAge());
    }
    
    private static class PersonIdComparator implements Comparator<Person> {
        @Override
        public int compare(Person o1, Person o2) {
            return o1.getId().compareTo(o2.getId());
        }        
    }
}
