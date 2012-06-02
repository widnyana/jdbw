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
package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.metadata.Nullability;
import com.googlecode.jdbw.server.DefaultDatabaseServerTraits;
import com.googlecode.jdbw.util.StringUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mabe02
 */
class MySQLTraits extends DefaultDatabaseServerTraits {

    //Package private
    MySQLTraits() {
    }
    
    private static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat mysqlDecimalDateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    @Override
    protected String formatDecimal(Object value) {
        if(value instanceof Date) {
            return mysqlDecimalDateFormat.format((Date) value);
        } else {
            return super.formatDecimal(value);
        }
    }
    
    private static final String HEXES = "0123456789ABCDEF";

    private String getHex(byte[] raw) {
        if(raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for(final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    @Override
    protected String formatBinary(Object value) {
        if(value instanceof byte[]) {
            return "UNHEX(" + getHex((byte[]) value) + ")";
        } else {
            return super.formatBinary(value);
        }
    }

    @Override
    public String escapeString(String string) {
        StringBuilder buffer = new StringBuilder(string.length());
        int start = 0, last = 0;
        int length = string.length();
        while(last < length) {
            char c = string.charAt(last);
            switch(c) {
                case '\n':
                case '\'':
                case '"':
                case '\t':
                case '\\':
                    buffer.append(string.substring(start, last));
                    start = last + 1;
                    buffer.append("\\").append(c);
                    break;
            }
            last++;
        }
        return buffer.append(string.substring(start, last)).toString();
    }

    @Override
    public String escapeIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public synchronized String formatDateTime(Date timestamp) {
        return timestampFormat.format(timestamp);
    }

    @Override
    public Object safeType(Column targetColumn, Object object) {
        if(targetColumn.getSqlType() == java.sql.Types.TIMESTAMP
                && object instanceof Date) {
            if("1900-01-01 00:00:00.0".equals(object.toString())) {
                return "1970-01-01 00:00:01";
            }
        }
        return super.safeType(targetColumn, object);
    }

    @Override
    public String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes) {
        return getCreateTableStatement(schemaName, name, columns, indexes, false);
    }

    public String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes, boolean decimalDates) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `").append(name).append("`(\n");
        for(Column column : columns) {
            sb.append("\t`").append(column.getName()).append("` ");
            sb.append(getMySQLDatatype(column, decimalDates));
            if(column.getNullable() == Nullability.NOT_NULLABLE) {
                sb.append(" NOT NULL");
            } else {
                sb.append(" NULL");
            }
            sb.append(",\n");
        }
        for(Index index : indexes) {
            if(index.isPrimaryKey()) {
                sb.append("\tPRIMARY KEY (`");
            } else if(index.isUnique()) {
                sb.append("\tUNIQUE INDEX `").append(index.getName()).append("`(`");
            } else {
                sb.append("\tINDEX `").append(index.getName()).append("`(`");
            }
            sb.append(StringUtils.concatenateStringList(index.getColumnNames(), "`,`"));
            sb.append("`),\n");
        }
        sb.delete(sb.length() - 2, sb.length()).append("\n");
        sb.append(") DEFAULT CHARACTER SET = UTF8, DEFAULT COLLATE = UTF8_BIN");
        return new String[]{sb.toString()};
    }

    @Override
    public String getDropTableStatement(String catalog, String schema, String tableName) {
        if(catalog != null) {
            return "DROP TABLE `" + catalog + "`.`" + tableName + "`";
        } else {
            return "DROP TABLE `" + tableName + "`";
        }
    }

    //This may be a column from any other database so don't make any assumptions!
    private String getMySQLDatatype(Column column, boolean decimalDates) {
        if(isBigDecimal(column.getSqlType())) {
            return "DECIMAL(" + column.getColumnSize() + ", " + column.getDecimalDigits() + ")";
        }
        if(isBinary(column.getSqlType())) {
            return "VARBINARY(" + column.getColumnSize() + ")";
        }
        if(isBoolean(column.getSqlType())) {
            return "BOOLEAN";
        }
        if(isDate(column.getSqlType())) {
            return "DATE";
        }
        if(isDatetime(column.getSqlType())) {
            if(decimalDates) {
                return "DECIMAL(17,3)";
            } else {
                return "DATETIME";
            }
        }
        if(isFloatingPoint(column.getSqlType())) {
            return "DOUBLE";
        }
        if(column.getSqlType() == java.sql.Types.TINYINT) {
            return "TINYINT";
        }
        if(column.getSqlType() == java.sql.Types.SMALLINT) {
            return "SMALLINT";
        }
        if(column.getSqlType() == java.sql.Types.INTEGER) {
            return "INT";
        }
        if(column.getSqlType() == java.sql.Types.BIGINT) {
            return "BIGINT";
        }
        if(isInteger(column.getSqlType())) { //Fallback
            return "BIGINT";
        }
        if(isString(column.getSqlType())) {
            return "VARCHAR(" + column.getColumnSize() + ")";
        }
        if(isTime(column.getSqlType())) {
            return "TIME";
        }
        return "<UNKNOWN TYPE>";
    }
}
