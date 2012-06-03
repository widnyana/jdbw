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
 * This interface can help you construct SQL that is compatible with a the
 * database server the trait belongs to. 
 * @author mabe02
 */
public interface DatabaseServerTraits {
    /**
     * @return True if this database can convert data of type fromSqlType to type toSqlType
     */
    boolean isCompatible(int fromSqlType, int toSqlType);
    boolean isCompatible(Column fromColumn, Column toColumn);
    
    /**
     * @return True if the value can be assigned to the column
     */
    boolean isCompatible(Object value, Column toColumn);
    
    /**
     * Returns a string with all special characters replaced with their proper
     * escape sequences. You don't need to do this for strings passed in as 
     * parameters, since the JDBC driver will do it for you.
     * @param string String to convert
     * @return The original string with special characters escaped
     */
    String escapeString(String string);
    
    /**
     * Takes the name of a database object (table, column, etc) and returns
     * the name wrapped in characters that marks it's an identifier. Use this
     * if, for example, your column names may be colliding with keywords on the
     * server.
     * @param identifier string to wrapp
     * @return The input string wrapped as an identifier
     */
    String escapeIdentifier(String identifier);
    String getApproximateRowCountQuery(String tableName);
    
    /**
     * @return What to put in front of a line to comment it out
     */
    String getSingleLineCommentPrefix();

    String formatTime(Date date);
    String formatDate(Date date);
    String formatDateTime(Date timestamp);
    String formatValue(Object value, int targetType);
    String formatValue(Object value, Column targetColumn);
    
    /**
     * Attempts to convert a particular value to an acceptable format. Given
     * a column on the server and the value we want to put into this column,
     * the method will try to convert the value into a format that is compatible
     * with the column.
     * @param targetColumn Column we want to insert into
     * @param object Object to insert
     * @return A compatible value for the target column
     */
    Object safeType(Column targetColumn, Object object);

    String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes);
    String getDropTableStatement(String catalog, String schema, String tableName);
}
