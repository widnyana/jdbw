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
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Berglund
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
        
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.put(elvis);        
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
    }

    /**
     * Test of putAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testPutAll() {
        System.out.println("putAll");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.putAll(createElvis(), createJaques());
        assertEquals(2, localStorage.getSize(Person.class));
        assertEquals(2, remoteStorage.getSize(Person.class));
        assertEquals(2, cachedRemoteStorage.getSize(Person.class));        
        
        cachedRemoteStorage.putAll(Arrays.asList(createElvis(), createJaques()));
        assertEquals(2, localStorage.getSize(Person.class));
        assertEquals(2, remoteStorage.getSize(Person.class));
        assertEquals(2, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.putAll(createElvis(), createSakamoto());
        assertEquals(3, localStorage.getSize(Person.class));
        assertEquals(3, remoteStorage.getSize(Person.class));
        assertEquals(3, cachedRemoteStorage.getSize(Person.class));
    }

    /**
     * Test of remove method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Person elvis = createElvis();
        
        cachedRemoteStorage.put(elvis);
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.remove(elvis);
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.put(elvis);
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.remove(Arrays.asList(elvis));
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.put(elvis);
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.remove(Person.class, elvis.getId());
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.put(elvis);
        assertEquals(1, localStorage.getSize(Person.class));
        assertEquals(1, remoteStorage.getSize(Person.class));
        assertEquals(1, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.remove(Person.class, Arrays.asList(elvis.getId()));
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
    }

    /**
     * Test of removeAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoveAll() {
        System.out.println("removeAll");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        cachedRemoteStorage.putAll(createElvis(), createJaques());
        assertEquals(2, localStorage.getSize(Person.class));
        assertEquals(2, remoteStorage.getSize(Person.class));
        assertEquals(2, cachedRemoteStorage.getSize(Person.class));   
        
        cachedRemoteStorage.removeAll(Person.class);
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
    }

    /**
     * Test of remoteGetSome method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetSome() {
        System.out.println("remoteGetSome");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        final Person sakamoto = createSakamoto();
        remoteStorage.put(sakamoto);
        
        assertNotNull(remoteStorage.get(Person.class, sakamoto.getId()));
        assertNull(localStorage.get(Person.class, sakamoto.getId()));
        assertNotNull(cachedRemoteStorage.remoteGetSome(Person.class, sakamoto.getId()));
        assertNotNull(localStorage.get(Person.class, sakamoto.getId()));
    }

    /**
     * Test of remoteGetAll method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetAll() {
        System.out.println("remoteGetAll");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        remoteStorage.putAll(createJaques(), createSakamoto());
        assertEquals(0, localStorage.getAll(Person.class).size());
        assertEquals(2, remoteStorage.getAll(Person.class).size());
        assertEquals(2, cachedRemoteStorage.remoteGetAll(Person.class).size());
        assertEquals(2, localStorage.getAll(Person.class).size());
    }

    /**
     * Test of remoteGetSize method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteGetSize() {
        System.out.println("remoteGetSize");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        
        remoteStorage.putAll(createJaques(), createSakamoto());
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(2, remoteStorage.getSize(Person.class));
        assertEquals(2, cachedRemoteStorage.remoteGetSize(Person.class));
        assertEquals(0, localStorage.getSize(Person.class));
    }

    /**
     * Test of remoteContains method, of class CachedRemoteObjectStorage.
     */
    @Test
    public void testRemoteContains() {
        System.out.println("remoteContains");
        assertEquals(0, localStorage.getSize(Person.class));
        assertEquals(0, remoteStorage.getSize(Person.class));
        assertEquals(0, cachedRemoteStorage.getSize(Person.class));
        final Person elvis = createElvis();
        
        remoteStorage.put(elvis);
        assertTrue(remoteStorage.contains(elvis));
        assertTrue(remoteStorage.contains(Person.class, elvis.getId()));
        assertFalse(localStorage.contains(elvis));
        assertFalse(localStorage.contains(Person.class, elvis.getId()));
        assertTrue(cachedRemoteStorage.remoteContains(elvis));
        assertTrue(cachedRemoteStorage.remoteContains(Person.class, elvis.getId()));
        assertFalse(localStorage.contains(elvis));
        assertFalse(localStorage.contains(Person.class, elvis.getId()));
    }
}
