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
package com.googlecode.jdbw.metadata;

/**
 * This enum represents the state of table column as to whether it can be 
 * assigned the {@code null} value or not. This special value has the meaning
 * of marking the absence of a value from the column. If the column is <i>not 
 * nullable</i>, it must always have a valid value that conforms to the data 
 * type.
 * 
 * @see Column
 * @author Martin Berglund
 */
public enum Nullability {

    /**
     * The column may contain {@code null} values
     */
    NULLABLE,
    
    /**
     * The column may not contain {@code null} values
     */
    NOT_NULLABLE,
    
    /**
     * This value means that either the JDBC driver or the database server does
     * not expose information of nullability to us
     */
    UNKNOWN
}
