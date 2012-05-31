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
package com.googlecode.jdbw.server;

import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.util.NullValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author mabe02
 */
public abstract class DefaultDatabaseServerTraits extends DatabaseServerTraits {

    private static final Pattern decimalTimestampPattern = Pattern.compile("..............\\...?.?");
    private static final DateFormat decimalTimestampFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    private static final DateFormat defaultTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat defaultTimeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public boolean isCompatible(int fromSqlType, int toSqlType) {
        if(fromSqlType == toSqlType) {
            return true;
        }

        Object exampleObject = createExampleObject(fromSqlType);
        return isCompatible(exampleObject, toSqlType);
    }

    @Override
    public boolean isCompatible(Column fromColumn, Column toColumn) {
        if(fromColumn.getSqlType() == toColumn.getSqlType()) {
            return true;
        }

        Object exampleObject = createExampleObject(fromColumn.getSqlType());
        return isCompatible(exampleObject, toColumn);
    }

    @Override
    public boolean isCompatible(Object value, Column toColumn) {
        return isCompatible(value, toColumn.getSqlType());
    }

    private boolean isCompatible(Object value, int toSqlType) {
        try {
            formatValue(value, toSqlType);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Object safeType(Column targetColumn, Object object) {
        if(object == null) {
            return NullValue.fromSqlType(targetColumn.getSqlType());
        }

        if(object instanceof BigDecimal && isDatetime(targetColumn.getSqlType())) {
            try {
                synchronized(decimalTimestampFormat) {
                    return decimalTimestampFormat.parse(((BigDecimal) object).toPlainString());
                }
            } catch(ParseException e) {
                return object;
            }
        }
        if(object instanceof Date && isBigDecimal(targetColumn.getSqlType())
                && targetColumn.getColumnSize() == 17 && targetColumn.getDecimalDigits() == 3) {
            synchronized(decimalTimestampFormat) {
                return new BigDecimal(decimalTimestampFormat.format((Date) object));
            }
        }

        return object;
    }

    @Override
    public String escapeString(String string) {
        return string;
    }

    @Override
    public String escapeIdentifier(String identifier) {
        return identifier;
    }

    @Override
    public String getApproximateRowCountQuery(String tableName) {
        return "SELECT COUNT(*) FROM " + tableName;
    }

    @Override
    public String getSingleLineCommentPrefix() {
        return "#";
    }

    public String formatDateTime(Date date) {
        synchronized(defaultTimestampFormat) {
            return defaultTimestampFormat.format(date);
        }
    }

    public String formatDate(Date date) {
        synchronized(defaultDateFormat) {
            return defaultDateFormat.format(date);
        }
    }

    public String formatTime(Date date) {
        synchronized(defaultTimeFormat) {
            return defaultTimeFormat.format(date);
        }
    }

    public String formatValue(Object value, Column targetColumn) {
        return formatValue(value, targetColumn.getSqlType());
    }

    @Override
    public String formatValue(Object value, int targetType) {
        if(value == null) {
            return "null";
        }

        if(isBigDecimal(targetType)) {
            return formatDecimal(value);
        }
        if(isBoolean(targetType)) {
            return formatBoolean(value);
        }
        if(isFloatingPoint(targetType)) {
            return formatFloatingPoint(value);
        }
        if(isString(targetType)) {
            return formatString(value);
        }
        if(isInteger(targetType)) {
            return formatInteger(value);
        }
        if(isDatetime(targetType)) {
            return formatDatetime(value);
        }
        if(isDate(targetType)) {
            return formatDate(value);
        }
        if(isTime(targetType)) {
            return formatTime(value);
        }
        if(isBinary(targetType)) {
            return formatBinary(value);
        }

        throw new IllegalArgumentException("Called DefaultDatabaseServerTraits.formatValue with an "
                + "unimplemented java.sql.Types constant (" + targetType + ")");
    }

    public static boolean isBigDecimal(int sqlType) {
        return sqlType == Types.NUMERIC
                || sqlType == Types.DECIMAL;
    }

    public static boolean isBoolean(int sqlType) {
        return sqlType == Types.BOOLEAN;
    }

    public static boolean isFloatingPoint(int sqlType) {
        return sqlType == Types.REAL
                || sqlType == Types.DOUBLE
                || sqlType == Types.FLOAT;
    }

    public static boolean isString(int sqlType) {
        return sqlType == Types.CHAR
                || sqlType == Types.CLOB
                || sqlType == Types.LONGNVARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NCLOB
                || sqlType == Types.NVARCHAR
                || sqlType == Types.VARCHAR;
    }

    public static boolean isInteger(int sqlType) {
        return sqlType == Types.TINYINT
                || sqlType == Types.SMALLINT
                || sqlType == Types.INTEGER
                || sqlType == Types.BIGINT;
    }

    public static boolean isDatetime(int sqlType) {
        return sqlType == Types.TIMESTAMP;
    }

    public static boolean isDate(int sqlType) {
        return sqlType == Types.DATE;
    }

    public static boolean isTime(int sqlType) {
        return sqlType == Types.TIME;
    }

    public static boolean isBinary(int sqlType) {
        return sqlType == Types.BINARY
                || sqlType == Types.BIT
                || sqlType == Types.BLOB
                || sqlType == Types.LONGVARBINARY
                || sqlType == Types.VARBINARY;
    }

    protected String formatDecimal(Object value) {
        if(value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if(value instanceof BigInteger) {
            return ((BigInteger) value).toString();
        }
        try {
            return new BigDecimal(value.toString()).toPlainString();
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a decimal");
        }
    }

    protected String formatBoolean(Object value) {
        if(value instanceof Boolean) {
            if(((Boolean) value).booleanValue()) {
                return "true";
            } else {
                return "false";
            }
        }
        if(value instanceof String) {
            if(Boolean.parseBoolean((String) value)) {
                return "true";
            } else {
                return "false";
            }
        }
        throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a boolean");
    }

    protected String formatFloatingPoint(Object value) {
        if(value instanceof Float || value instanceof Double) {
            return value.toString();
        }
        if(value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }

        try {
            return new BigDecimal(value.toString()).toPlainString();
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a floating point");
        }
    }

    protected String formatString(Object value) {
        return "'" + escapeString(value.toString()) + "'";
    }

    protected String formatInteger(Object value) {
        if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return value.toString();
        }
        if(value instanceof BigInteger) {
            return ((BigInteger) value).toString();
        }

        try {
            return new BigDecimal(value.toString()).toBigInteger().toString();
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to an integer");
        }
    }

    protected String formatDatetime(Object value) {
        if(value instanceof Date) {
            return "'" + formatDateTime((Date) value) + "'";
        }
        if(value instanceof BigDecimal
                && decimalTimestampPattern.matcher(((BigDecimal) value).toPlainString()).matches()) {
            try {
                synchronized(decimalTimestampFormat) {
                    return formatDatetime(decimalTimestampFormat.parse(((BigDecimal) value).toPlainString()));
                }
            } catch(ParseException e) {
            }
        }


        throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a datetime");
    }

    protected String formatDate(Object value) {
        if(value instanceof Date) {
            return "'" + formatDate((Date) value) + "'";
        }

        throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a date");
    }

    protected String formatTime(Object value) {
        if(value instanceof Date) {
            return "'" + formatTime((Date) value) + "'";
        }

        throw new IllegalArgumentException("Could not format type " + value.getClass().getName() + " to a time");
    }

    protected String formatBinary(Object value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected Object createExampleObject(int sqlType) {
        if(isBigDecimal(sqlType)) {
            return new BigDecimal("20100101000000.000");
        } else if(isBinary(sqlType)) {
            return new byte[1];
        } else if(isBoolean(sqlType)) {
            return true;
        } else if(isDate(sqlType)) {
            return new Date();
        } else if(isDatetime(sqlType)) {
            return new Date();
        } else if(isFloatingPoint(sqlType)) {
            return 17.3;
        } else if(isInteger(sqlType)) {
            return 17;
        } else if(isString(sqlType)) {
            return "sjutton";
        } else if(isTime(sqlType)) {
            return new Date();
        } else {
            throw new IllegalArgumentException("Called DefaultDatabaseServerTraits.createExampleObject with an "
                    + "unimplemented java.sql.Types constant (" + sqlType + ")");
        }
    }

    @Override
    public String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDropTableStatement(String catalog, String schema, String tableName) {
        return "DROP TABLE "
                + (catalog != null ? catalog + "." : "")
                + (schema != null ? schema + "." : "")
                + tableName;
    }
}
