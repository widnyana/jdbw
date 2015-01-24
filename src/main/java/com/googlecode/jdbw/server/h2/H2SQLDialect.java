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
package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import com.googlecode.jdbw.metadata.Nullability;
import com.googlecode.jdbw.server.DefaultSQLDialect;
import com.googlecode.jdbw.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * H2 SQL dialect specific traits
 * @author Martin Berglund
 */
public class H2SQLDialect extends DefaultSQLDialect {
    
    @Override
    public String escapeIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }
    
    @Override
    public String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes) {
        List<String> SQL = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE \"").append(name).append("\"(\n");
        for(Column column : columns) {
            sb.append("\t\"").append(column.getName()).append("\" ");
            sb.append(getH2Datatype(column));
            if(column.getNullable() == Nullability.NOT_NULLABLE) {
                sb.append(" NOT NULL");
            } else {
                sb.append(" NULL");
            }
            sb.append(",\n");
        }
        sb.delete(sb.length() - 2, sb.length()).append("\n");
        sb.append(")");
        SQL.add(sb.toString());
                
        for(Index index : indexes) {
            sb = new StringBuilder("CREATE ");
            if(index.isPrimaryKey()) {
                sb.append("PRIMARY KEY ");
            }
            else if(index.isUnique()) {
                sb.append("UNIQUE INDEX \"").append(name).append(".").append(index.getName()).append("\" ");
            } 
            else {
                sb.append("INDEX \"").append(name).append(".").append(index.getName()).append("\" ");
            }
            sb.append(" ON \"").append(name).append("\" (\"");
            sb.append(StringUtils.concatenateStringList(index.getColumnNames(), "\",\""));
            sb.append("\")");
            SQL.add(sb.toString());
        }
        return SQL.toArray(new String[SQL.size()]);
    }
    
    //This may be a column from any other database so don't make any assumptions!
    private String getH2Datatype(Column column) {
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
            return "DATETIME";
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
