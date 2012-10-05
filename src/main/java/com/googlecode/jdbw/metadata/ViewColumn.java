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
     * @return The table owning this column
     */
    public View getView() {
        return view;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj instanceof ViewColumn == false) {
            return false;
        }

        ViewColumn other = (ViewColumn) obj;
        return getView().getSchema().getCatalog().getName().equals(other.getView().getSchema().getCatalog().getName())
                && getView().getSchema().getName().equals(other.getView().getSchema().getName())
                && getView().getName().equals(other.getView().getName())
                && getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
