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

package com.googlecode.jdbw;

import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mabe02
 */
public interface DatabaseServerTraits {
    boolean isCompatible(int fromSqlType, int toSqlType);
    boolean isCompatible(Column fromColumn, Column toColumn);
    boolean isCompatible(Object value, Column toColumn);
    String escapeString(String string);
    String escapeIdentifier(String identifier);
    String getApproximateRowCountQuery(String tableName);
    String getSingleLineCommentPrefix();

    String formatTime(Date date);
    String formatDate(Date date);
    String formatDateTime(Date timestamp);
    String formatValue(Object value, int targetType);
    String formatValue(Object value, Column targetColumn);
    Object safeType(Column targetColumn, Object object);

    String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes);
    String getDropTableStatement(String catalog, String schema, String tableName);
}
