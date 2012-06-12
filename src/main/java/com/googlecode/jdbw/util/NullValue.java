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
 * Copyright (C) 2007-2012 mabe02
 */

package com.googlecode.jdbw.util;

/**
 *
 * @author mabe02
 */
public class NullValue
{
    private NullValue()
    {
    }

    public static Object fromSqlType(int sqlType)
    {
        switch(sqlType) {
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
                return new Integer();

            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.REAL:
                return new Double();

            case java.sql.Types.CHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                return new String();

            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return new Decimal();

            case java.sql.Types.TIMESTAMP:
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
                return new Timestamp();

            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return new Binary();
        }
        return null;
    }

    public static class Integer extends NullValue
    {
    }

    public static class Double extends NullValue
    {
    }

    public static class String extends NullValue
    {
    }

    public static class Decimal extends NullValue
    {
    }

    public static class Timestamp extends NullValue
    {
    }

    public static class Binary extends NullValue
    {
    }
}
