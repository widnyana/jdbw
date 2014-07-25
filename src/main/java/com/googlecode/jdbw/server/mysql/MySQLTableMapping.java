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
package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.objectstorage.TableMapping;
import com.googlecode.jdbw.objectstorage.impl.DelegatingTableMapping;

/**
 * Special table mapping class for MySQL which supports the 
 * <b>INSERT INTO ... ON DUPLICATE UPDATE ...</b> syntax.
 * @author Martin Berglund
 */
class MySQLTableMapping extends DelegatingTableMapping {

    private final MySQLDialect dialect;
    
    MySQLTableMapping(TableMapping backend) {
        super(backend);
        dialect = new MySQLDialect();
    }

    String getMySQLInsertOrUpdate() {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(dialect.escapeIdentifier(getTableName()));
        sb.append(" (");
        sb.append(dialect.escapeIdentifier(getColumnName("id")));
        for(String fieldName: getFieldNames()) {
            sb.append(", ").append(dialect.escapeIdentifier(getColumnName(fieldName)));
        }
        sb.append(") VALUES(?");
        for(int i = 0; i < getFieldNames().size(); i++) {
            sb.append(", ?");
        }
        sb.append(") ON DUPLICATE KEY UPDATE ");
        for(String fieldName: getFieldNames()) {
            sb.append(dialect.escapeIdentifier(getColumnName(fieldName)));
            sb.append(" = VALUES(");
            sb.append(dialect.escapeIdentifier(getColumnName(fieldName)));
            sb.append("), ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }    
}
