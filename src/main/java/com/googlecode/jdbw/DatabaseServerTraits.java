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
 * Copyright (C) 2009-2012 mabe02
 */

package com.googlecode.jdbw;

import com.googlecode.jdbw.metadata.Column;

/**
 * This interface holds some methods dealing with various database type specific
 * behaviors that are not strictly related to the SQL dialect
 * @author mabe02
 */
public interface DatabaseServerTraits {
    /**
     * @return True if this database can convert data of type fromSqlType to type toSqlType
     */
    boolean isCompatible(int fromSqlType, int toSqlType);
        
    /**
     * Attempts to convert a particular value to an acceptable format. Given
     * a column on the server and the value we want to put into this column,
     * the method will try to convert the value into a format that is compatible
     * with the column.
     * @param targetColumn Column we want to insert into
     * @param object Object to insert
     * @return A compatible value for the target column
     */
    Object safeType(Column targetColumn, Object object);
}
