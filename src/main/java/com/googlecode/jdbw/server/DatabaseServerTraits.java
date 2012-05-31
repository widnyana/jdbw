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
import java.util.Date;
import java.util.List;

/**
 *
 * @author mabe02
 */
public abstract class DatabaseServerTraits {
    public abstract boolean isCompatible(int fromSqlType, int toSqlType);
    public abstract boolean isCompatible(Column fromColumn, Column toColumn);
    public abstract boolean isCompatible(Object value, Column toColumn);
    public abstract String escapeString(String string);
    public abstract String escapeIdentifier(String identifier);
    public abstract String getApproximateRowCountQuery(String tableName);
    public abstract String getSingleLineCommentPrefix();

    public abstract String formatTime(Date date);
    public abstract String formatDate(Date date);
    public abstract String formatDateTime(Date timestamp);
    public abstract String formatValue(Object value, int targetType);
    public abstract String formatValue(Object value, Column targetColumn);
    public abstract Object safeType(Column targetColumn, Object object);

    public abstract String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes);
    public abstract String getDropTableStatement(String catalog, String schema, String tableName);
}
