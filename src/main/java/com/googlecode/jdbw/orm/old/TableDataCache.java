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
package com.googlecode.jdbw.orm.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDataCache<U> {
    private final String tableName;
    private final List<String> fieldNames;
    private final List<Class> fieldTypes;
    private final Map<U, Object[]> KeyToRowData;

    public TableDataCache(
                String tableName, 
                List<String> fieldNames, 
                List<Class> fieldTypes) {
        
        if(tableName == null) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null tableName");
        }
        if(fieldNames == null || fieldNames.contains(null)) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldNames "
                    + "(or list has a null element)");
        }
        if(fieldTypes == null || fieldTypes.contains(null)) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...) with null fieldTypes "
                    + "(or list has a null element)");
        }
        if(fieldNames.size() != fieldTypes.size()) {
            throw new IllegalArgumentException("Illegal calling TableDataCache(...), "
                    + "fieldNames has " + fieldNames.size() + " elements but fieldTypes has " 
                    + fieldTypes.size());
        }
        
        this.tableName = tableName;
        this.fieldNames = Collections.unmodifiableList(new ArrayList<String>(fieldNames));
        this.fieldTypes = Collections.unmodifiableList(new ArrayList<Class>(fieldTypes));
        this.KeyToRowData = new HashMap<U, Object[]>();
    }
    
    
}
