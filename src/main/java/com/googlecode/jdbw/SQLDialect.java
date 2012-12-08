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
package com.googlecode.jdbw;

import com.googlecode.jdbw.metadata.Column;
import com.googlecode.jdbw.metadata.Index;
import java.util.Date;
import java.util.List;

/**
 * This class contains methods to assist you in formatting SQL queries adjusted
 * to the target database type
 * @author Martin Berglund
 */
public interface SQLDialect {

    /**
     * Takes the name of a database object (table, column, etc) and returns
     * the name wrapped in characters that marks it's an identifier. Use this
     * if, for example, your column names may be colliding with keywords on the
     * server.
     * @param identifier string to wrapp
     * @return The input string wrapped as an identifier
     */
    String escapeIdentifier(String identifier);

    /**
     * Returns a string with all special characters replaced with their proper
     * escape sequences. You don't need to do this for strings passed in as
     * parameters, since the JDBC driver will do it for you.
     * @param string String to convert
     * @return The original string with special characters escaped
     */
    String escapeString(String string);

    /**
     * Formats a java Date into the preferred date string format for this database
     * server type, probably YYYY-MM-DD
     * @param date Date to format
     * @return Date formatted as a string
     */
    String formatDate(Date date);

    /**
     * Formats a java Date into the preferred date time string format for this database
     * server type, probably YYYY-MM-DD HH:MM:SS.ZZZ
     * @param timestamp Date to format
     * @return Date formatted as a string
     */
    String formatDateTime(Date timestamp);

    /**
     * Formats a java Date into the preferred time string format for this database
     * server type, probably HH:MM:SS.ZZZ
     * @param date Date to format
     * @return Date formatted as a string
     */
    String formatTime(Date date);

    /**
     * Formats a generic object into a String that can be inserted into a 
     * dynamically constructed SQL statement. You'll need to supply the type
     * you want the value to be in the database context through the targetType
     * parameter. 
     * 
     * Note: Throws IllegalArgumentException is the objects cannot be formatted
     * @param value Value to be formatted
     * @param targetType java.sql.Types constant of the type you want to format
     * the value as
     * @return String with the value formatted as the specified type
     */
    String formatValue(Object value, int targetType);

    String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes);

    String getDropTableStatement(String catalog, String schema, String tableName);

    /**
     * @return What to put in front of a line to comment it out
     */
    String getSingleLineCommentPrefix();
    
    /**
     * @return The name of the 'default' schema (PUBLIC in H2 and Postgres, dbo in Sybase, etc)
     */
    String getDefaultSchemaName();
    
    /**
     * @return True if this database can convert data of type fromSqlType to type toSqlType
     */
    boolean isCompatible(int fromSqlType, int toSqlType);
        
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
}
