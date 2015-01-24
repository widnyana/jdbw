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
 * A column is a part of a database table, holding a specific value for every
 * row in the table. The column will have a determined type and depending on
 * the type could also have additional data associated with it, such as maximum
 * string length or decimal precision. A column is also either <i>nullable</i> (can 
 * have the {@code null} value, representing the absence of a value) or <i>not
 * nullable</i> (trying to assign {@code null} will give an error). 
 * 
 * <p>Normally you don't construct {@code Column} objects yourself, rather you
 * get them by using methods on other objects that holds columns.
 * 
 * @see View
 * @see Column
 * @author Martin Berglund
 */
public class ViewColumn extends Column {
    private final View view;

    /**
     * Creates a ViewColumn object based on manually entered information
     * @param view View the column belongs to
     * @param ordinalPosition Index of the column in the view, where 1 means the first column
     * @param columnName Name of the column
     * @param sqlType Data type of the column, please use constants from java.sql.Types
     * @param typeName Native name of the data type, as the database server understands it
     * @param columnSize Maximum size of the column, such as maximum number of characters for String-type columns and
     *                   maximum precision for decimal/numeric type column
     * @param decimalDigits Only relevant for decimal type columns, this value signifies number of digits to the right
     *                      of the dot (the "scale") that this column uses
     * @param nullable  If the column is nullable or not, use one of the two constants
     *                  {@code DatabaseMetaData.columnNullable} and {@code DatabaseMetaData.columnNoNulls}
     * @param autoIncrement The value to use for auto-increment, if the column is auto-incremented then you should
     *                      probably set "YES" here
     */
    public ViewColumn(
            View view, 
            int ordinalPosition, 
            String columnName, 
            int sqlType, 
            String typeName, 
            int columnSize, 
            int decimalDigits, 
            int nullable, 
            String autoIncrement) {
        
        super(ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
        this.view = view;
    }

    /**
     * Returns the view that is containing this column
     * @return The view owning this column
     */
    public View getView() {
        return view;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ViewColumn)) {
            return false;
        }

        ViewColumn other = (ViewColumn) obj;
        return getView().getSchema().getCatalog().getName().equals(other.getView().getSchema().getCatalog().getName())
                && getView().getSchema().getName().equals(other.getView().getSchema().getName())
                && getView().getName().equals(other.getView().getName())
                && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
