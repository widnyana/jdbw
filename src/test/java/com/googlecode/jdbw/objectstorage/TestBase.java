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
import java.util.Date;
import org.joda.time.DateMidnight;

public abstract class TestBase {
    
    protected static interface Person extends Storable<Integer> {
        String getName();
        int getAge();
        Date getBirthday();

        static interface Builder extends ObjectBuilder<Person>, Person {
            Person.Builder setName(String name);
            Person.Builder setAge(int age);
            Person.Builder setBirthday(Date birthday);
        }
    }
                
    
    protected final static Date ELVIS_BIRTHDAY = new DateMidnight("1935-01-08").toDate();
    protected final static Date JAQUES_BIRTHDAY = new DateMidnight("1929-04-08").toDate();
    protected final static Date SAKAMOTO_BIRTHDAY = new DateMidnight("1941-11-10").toDate();

    protected Person createElvis() {
        return createElvis(new DefaultObjectBuilderFactory());
    }
    
    protected Person createElvis(ObjectBuilderFactory builderFactory) {
        return builderFactory.newObject(Person.Builder.class, 1)
                .setAge(42)
                .setName("Elvis Presley")
                .setBirthday(ELVIS_BIRTHDAY)
                .build();
    }

    protected Person createJaques() {
        return createJaques(new DefaultObjectBuilderFactory());
    }
    
    protected Person createJaques(ObjectBuilderFactory builderFactory) {
        return builderFactory.newObject(Person.Builder.class, 2)
                .setAge(49)
                .setName("Jaques Brel")
                .setBirthday(JAQUES_BIRTHDAY)
                .build();
    }

    protected Person createSakamoto() {
        return createSakamoto(new DefaultObjectBuilderFactory());
    }
    
    protected Person createSakamoto(ObjectBuilderFactory builderFactory) {
        return builderFactory.newObject(Person.Builder.class, 3)
                .setAge(43)
                .setName("Kyo Sakamoto")
                .setBirthday(SAKAMOTO_BIRTHDAY)
                .build();
    }
}
