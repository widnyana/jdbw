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
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A column is a part of a database table or a view, holding a specific value for every
 * row in the table/view. The column will have a determined type and depending on
 * the type could also have additional data associated with it, such as maximum
 * string length or decimal precision. A column is also either <i>nullable</i> (can 
 * have the {@code null} value, representing the absence of a value) or <i>not
 * nullable</i> (trying to assign {@code null} will give an error). 
 * 
 * <p/>Normally you don't construct {@code Column} objects yourself, rather you
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

    /**
     * Creates a new column object based on specified values. This constructor is designed to match the output of the
     * JDBC's ResultSetMetaData so it's not very friendly to use manually.
     * @param ordinalPosition Index of the column in the table, where 1 means the first column
     * @param columnName Name of the column
     * @param sqlType Data type of the column
     * @param typeName Name of the data type, this is database server specific
     * @param columnSize Size of the column (length of strings, precision of decimals, etc), or 0 if not applicable
     * @param decimalDigits For decimal columns, this defines the scale (number of digits to the right of the dot),
     *                      otherwise 0
     * @param nullable If the column is nullable or not, use one of the two constants
     *                  {@code DatabaseMetaData.columnNullable} and {@code DatabaseMetaData.columnNoNulls}
     * @param autoIncrement The value to use for auto-increment, if the column is auto-incremented then you should
     *                      probably set "YES" here
     */
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
     * Returns the auto increment value for the column, which is somewhat database server specific. In general, it seems
     * like "YES" is the normal value for auto-incremented columns and anything else should probably be assumed to mean
     * "no".
     * @return The JDBC value of IS_AUTOINCREMENT for this column
     */
    public String getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * Returns the name of the column
     * @return Name of the column
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the max size of this column. For string type columns it means number of characters supported and for
     * decimal columns it means the total precision. For all other types, this is undefined (see JDBC documentation).
     * @return The max size of the column
     */
    public int getColumnSize() {
        return columnSize;
    }

    /**
     * For decimal or numeric columns, this value is the max number of fractional digits, for all other types undefined.
     * @return The scale part of decimal/numeric columns
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * Returns a value for whether the column can be null or not. Please note that there is a third value that may come
     * out from this method, unknown, which can show up if your database server is cruel or you are inspecting a column
     * that in some way the database cannot inspect the nullability of.
     * @return Nullability of this column
     */
    public Nullability getNullable() {
        return nullable;
    }

    /**
     * Gives back the index this column has in the table, indexed from 1 so the first column has ordinal 1, the second
     * has 2, and so on.
     * @return Index of this column in its table, where the first column has index 1
     */
    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * Returns the data type of this column, expressed as an integer which matches a constant in java.sql.Types.
     * 
     * @see java.sql.Types
     * @return Datatype of the column, matching a JDBC constant in {@code java.sql.Types}.
     */
    public int getSqlType() {
        return sqlType;
    }

    /**
     * Returns what the database server calls this data type in its native domain. This value is generally not always
     * cross-compatible as different servers may use their own names for certain data types.
     * @return The native name of the data type for this column, as the server presents it to the JDBC driver
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
        return "Column{" + getName() + " " + getNativeTypeName() + "(" + getColumnSize() + ")}";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + this.sqlType;
        hash = 37 * hash + this.columnSize;
        hash = 37 * hash + this.decimalDigits;
        hash = 37 * hash + (this.nullable != null ? this.nullable.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Column other = (Column) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.sqlType != other.sqlType) {
            return false;
        }
        final Set<Integer> sizeMatters = 
                new HashSet<Integer>(
                        Arrays.asList(
                                //String types
                                Types.CHAR, Types.VARCHAR, Types.BINARY, Types.VARBINARY, Types.NCHAR, Types.NVARCHAR,
                                //Numeric types
                                Types.DECIMAL, Types.NUMERIC));
        if(sizeMatters.contains(this.sqlType)) {
            if (this.columnSize != other.columnSize) {
                return false;
            }
            if(this.sqlType == Types.DECIMAL || this.sqlType == Types.NUMERIC) {
                if (this.decimalDigits != other.decimalDigits) {
                    return false;
                }
            }
        }
        return this.nullable == other.nullable;
    }
}
