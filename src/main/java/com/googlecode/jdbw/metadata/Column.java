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

import java.sql.DatabaseMetaData;

/**
 * A column is a part of a database table or a view, holding a specific value for every
 * row in the table/view. The column will have a determined type and depending on
 * the type could also have additional data associated with it, such as maximum
 * string length or decimal precision. A column is also either <i>nullable</i> (can 
 * have the {@code null} value, representing the absence of a value) or <i>not
 * nullable</i> (trying to assign {@code null} will give an error). 
 * 
 * <p>Normally you don't construct {@code Column} objects yourself, rather you
 * get them by using methods on other objects that holds columns.
 * 
 * @see Table
 * @see View
 * @see TableColumn
 * @see ViewColumn
 * @see Index
 * @author Martin Berglund
 */
public abstract class Column implements Comparable<Column> {

    private final Integer ordinalPosition;
    private final String name;
    private final int sqlType;
    private final String typeName;
    private final int columnSize;
    private final int decimalDigits;
    private final Nullability nullable;
    private final String autoIncrement;

    public Column(int ordinalPosition, String columnName, int sqlType, String typeName,
            int columnSize, int decimalDigits, int nullable, String autoIncrement) {
        this.ordinalPosition = ordinalPosition;
        this.name = columnName;
        this.sqlType = sqlType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
        if(nullable == DatabaseMetaData.columnNullable) {
            this.nullable = Nullability.NULLABLE;
        } else if(nullable == DatabaseMetaData.columnNoNulls) {
            this.nullable = Nullability.NOT_NULLABLE;
        } else {
            this.nullable = Nullability.UNKNOWN;
        }
        this.autoIncrement = autoIncrement;
    }

    /**
     * @return The JDBC value of IS_AUTOINCREMENT for this column
     */
    public String getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * @return Name of the column
     */
    public String getName() {
        return name;
    }

    /**
     * @return The max size of the column, for string type columns, or the max
     * total number of digits for decimal types. For all other types, this is 
     * undefined.
     */
    public int getColumnSize() {
        return columnSize;
    }

    /**
     * @return For decimal or numeric columns, the max number of fractional 
     * digits, for all other types undefined
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * @return Nullability of this column
     */
    public Nullability getNullable() {
        return nullable;
    }

    /**
     * @return Index of this column in its table, where the first column has 
     * index 1
     */
    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * Returns the datatype of this column, expressed as an integer which 
     * matches a constant in java.sql.Types.
     * 
     * @see java.sql.Types
     * @return Datatype of the column, matching a JDBC constant in {@code java.sql.Types}.
     */
    public int getSqlType() {
        return sqlType;
    }

    /**
     * @return The native name of the data type for this column, as the server
     * presents it to the JDBC driver
     */
    public String getNativeTypeName() {
        return typeName;
    }

    @Override
    public int compareTo(Column o) {
        return ordinalPosition.compareTo(o.ordinalPosition);
    }

    @Override
    public String toString() {
        return getName() + " " + getNativeTypeName() + "(" + getColumnSize() + ")";
    }
}
