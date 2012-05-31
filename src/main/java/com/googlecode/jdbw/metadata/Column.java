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
package com.googlecode.jdbw.metadata;

import java.sql.DatabaseMetaData;

/**
 *
 * @author mabe02
 */
public class Column implements Comparable<Column> {

    private final Integer ordinalPosition;
    private final String name;
    private final int sqlType;
    private final String typeName;
    private final int columnSize;
    private final int decimalDigits;
    private final Nullability nullable;
    private final String autoIncrement;
    private final Table table;

    public Column(int ordinalPosition, String columnName, int sqlType, String typeName,
            int columnSize, int decimalDigits, int nullable, String autoIncrement, Table table) {
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
        this.table = table;
    }

    public String getAutoIncrement() {
        return autoIncrement;
    }

    public String getName() {
        return name;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public Nullability getNullable() {
        return nullable;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public int getSqlType() {
        return sqlType;
    }

    public String getNativeTypeName() {
        return typeName;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public int compareTo(Column o) {
        return ordinalPosition.compareTo(o.ordinalPosition);
    }

    @Override
    public String toString() {
        return table.getName() + "." + getName() + " " + getNativeTypeName() + "(" + getColumnSize() + ")";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj instanceof Column == false) {
            return false;
        }

        Column other = (Column) obj;
        return getTable().getSchema().getCatalog().getName().equals(other.getTable().getSchema().getCatalog().getName())
                && getTable().getSchema().getName().equals(other.getTable().getSchema().getName())
                && getTable().getName().equals(other.getTable().getName())
                && getName().equals(getName());
    }
}
