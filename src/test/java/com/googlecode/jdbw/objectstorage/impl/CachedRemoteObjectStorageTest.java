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

import com.googlecode.jdbw.objectstorage.ObjectStorage;
import com.googlecode.jdbw.objectstorage.TestBase;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martin
 */
public class CachedRemoteObjectStorageTest extends TestBase {
    
    private final ObjectStorage localStorage;
    private final ObjectStorage remoteStorage;
    private final CachedRemoteObjectStorage cachedRemoteStorage;
    
    public CachedRemoteObjectStorageTest() {
        this.localStorage = new DefaultObjectStorage();
        this.remoteStorage = new DefaultObjectStorage();
        this.cachedRemoteStorage = new CachedRemoteObjectStorage(remoteStorage, localStorage);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cachedRemoteStorage.register(Person.class);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of register method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRegister() {
        System.out.println("register");
        
        localStorage.getAll(Person.class);
        remoteStorage.getAll(Person.class);
        cachedRemoteStorage.getAll(Person.class);
    }

    /**
     * Test of put method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        Person elvis = createElvis();
        cachedRemoteStorage.put(elvis);
        
        assertTrue(localStorage.contains(elvis));
        assertTrue(remoteStorage.contains(elvis));
        assertTrue(cachedRemoteStorage.contains(elvis));    
        
        assertEquals(elvis, localStorage.get(Person.class, elvis.getId()));
        assertEquals(elvis, remoteStorage.get(Person.class, elvis.getId()));
        assertEquals(elvis, cachedRemoteStorage.get(Person.class, elvis.getId()));
    }

    /**
     * Test of putAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testPutAll() {
        System.out.println("putAll");
        CachedRemoteObjectStorage instance = null;
        List expResult = null;
        List result = instance.putAll(null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
    }

    /**
     * Test of removeAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoveAll() {
        System.out.println("removeAll");
    }

    /**
     * Test of remoteGetSome method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetSome() {
        System.out.println("remoteGetSome");
    }

    /**
     * Test of remoteGetAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetAll() {
        System.out.println("remoteGetAll");
    }

    /**
     * Test of remoteGetSize method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetSize() {
        System.out.println("remoteGetSize");
    }
}
