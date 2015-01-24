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

import com.googlecode.jdbw.objectstorage.TestBase;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
public class DefaultFieldMappingTest extends TestBase {
    
    public DefaultFieldMappingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getObjectType method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetObjectType() {
        System.out.println("getObjectType");
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        Class expResult = Person.class;
        Class result = instance.getObjectType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldName method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldName_Method() throws NoSuchMethodException {
        System.out.println("getFieldName");
        Method method = Person.class.getMethod("getAge");
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        String expResult = "age";
        String result = instance.getFieldName(method);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldName method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldName_String() {
        System.out.println("getFieldName");
        String methodName = "getAge";
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        String expResult = "age";
        String result = instance.getFieldName(methodName);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldIndex method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldIndex_String() {
        System.out.println("getFieldIndex");
        String fieldName = "age";
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        int expResult = 0;
        int result = instance.getFieldIndex(fieldName);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldIndex method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldIndex_Method() throws NoSuchMethodException {
        System.out.println("getFieldIndex");
        Method method = Person.class.getMethod("getAge");
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        int expResult = 0;
        int result = instance.getFieldIndex(method);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldNames method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldNames() {
        System.out.println("getFieldNames");
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        Set<String> expResult = new HashSet<String>(Arrays.asList("age", "name", "birthday"));
        Set<String> result = new HashSet<String>(instance.getFieldNames());
        assertEquals(expResult, result);
    }

    /**
     * Test of getFieldTypes method, of class DefaultFieldMapping.
     */
    @Test
    public void testGetFieldTypes() {
        System.out.println("getFieldTypes");
        DefaultFieldMapping instance = new DefaultFieldMapping(Person.class);
        Set<Class> expResult = new HashSet<Class>(Arrays.<Class>asList(int.class, String.class, Date.class));
        Set<Class> result = new HashSet<Class>(instance.getFieldTypes());
        assertEquals(expResult, result);
    }
}
