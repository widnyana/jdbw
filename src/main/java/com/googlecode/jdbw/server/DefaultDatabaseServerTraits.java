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

import com.googlecode.jdbw.DatabaseServerTraits;
import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.util.NullValue;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mabe02
 */
public abstract class DefaultDatabaseServerTraits extends DefaultSQLDialect implements DatabaseServerTraits {

    private static final DateFormat decimalTimestampFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    @Override
    public boolean isCompatible(int fromSqlType, int toSqlType) {
        if(fromSqlType == toSqlType) {
            return true;
        }

        Object exampleObject = createExampleObject(fromSqlType);
        return isCompatible(exampleObject, toSqlType);
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
}
