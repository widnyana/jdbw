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
 * This class is a null value representation when dealing with SQL parameters.
 * The reason why this is needed is that some database servers requires you to
 * supply what 'type' of null (as in, is it a {@code varchar} null, an {@code int} 
 * null or something else) you are giving. Trying to assign null of a wrong type 
 * will give you an SQLException. 
 * 
 * <p>Since the Java {@code null} keyword has no type information, when you supply
 * it to a JDBW class database method (for example, {@code SQLWorker.query(...)} 
 * or {@code SQLExecutor.execute(...)}) in the parameter list, type 
 * {@code varchar} will be associated with this null. If this is incompatible
 * with your database server/table/column, you can instead of null pass in an
 * instance of one of the subclasses of this class. There isn't a subclass for 
 * every conceivable datatype, but there should be one that is compatible with
 * what you need.
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

    /**
     * Represents a {@code null} integer value.
     * 
     * @see NullValue
     */
    public static class Integer extends NullValue
    {
    }

    /**
     * Represents a {@code null} double value.
     * 
     * @see NullValue
     */
    public static class Double extends NullValue
    {
    }

    /**
     * Represents a {@code null} varchar value.
     * 
     * @see NullValue
     */
    public static class String extends NullValue
    {
    }

    /**
     * Represents a {@code null} decimal value.
     * 
     * @see NullValue
     */
    public static class Decimal extends NullValue
    {
    }

    /**
     * Represents a {@code null} timestamp value.
     * 
     * @see NullValue
     */
    public static class Timestamp extends NullValue
    {
    }

    /**
     * Represents a {@code null} binary value.
     * 
     * @see NullValue
     */
    public static class Binary extends NullValue
    {
    }
}
