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
 * This class contains methods to assist you in formatting SQL queries adjusted to the target database type. In most
 * cases, using parameter substition is good enough to deal with database server oddities and protecting against things
 * like SQL injection attacks or difference in date formatting. However, sometimes you need to write the SQL manually
 * and specify the values directly and in order to do that, this interface exposes methods that can help you.
 *
 * @author Martin Berglund
 */
public interface SQLDialect {

    /**
     * Takes the name of a database object (table, column, etc) and returns the name wrapped in characters that marks
     * it's an identifier. Use this if, for example, your column names may be colliding with keywords on the server.
     * @param identifier string to wrapp
     * @return The input string wrapped as an identifier
     */
    String escapeIdentifier(String identifier);

    /**
     * Returns a string with all special characters replaced with their proper escape sequences. You don't need to do
     * this for strings passed in as parameters, since the JDBC driver will do it for you, but whenever you are manually
     * assigning a string value, you should <b>always</b> make sure the string is properly escaped as you could
     * otherwise be at risk of an SQL injection attack.
     * @param string String to escape
     * @return The original string with special characters escaped
     */
    String escapeString(String string);

    /**
     * Formats a java Date into the preferred date string format for this database server type, probably YYYY-MM-DD
     * @param date Date to format
     * @return Date formatted as a string
     */
    String formatDate(Date date);

    /**
     * Formats a java Date into the preferred date time string format for this database server type, probably
     * YYYY-MM-DD HH:MM:SS.ZZZ
     * @param timestamp Date to format
     * @return Date formatted as a string
     */
    String formatDateTime(Date timestamp);

    /**
     * Formats a java Date into the preferred time string format for this database server type, probably HH:MM:SS.ZZZ
     * @param date Date to format
     * @return Date formatted as a string
     */
    String formatTime(Date date);

    /**
     * Formats a generic object into a String that can be inserted into a dynamically constructed SQL statement. You'll
     * need to supply the type you want the value to be in the database context through the targetType parameter. If the
     * requested target type is a character-based type, the returned value will contain the string quotes and it will be
     * properly escaped, so you don't need to think about that.
     * 
     * Note: Throws IllegalArgumentException is the objects cannot be formatted
     * @param value Value to be formatted
     * @param targetType java.sql.Types constant of the type you want to format the value as
     * @return String with the value formatted as the specified type
     */
    String formatValue(Object value, int targetType);

    /**
     * Generates an array of statements requires to create a table using the supplied specifications. JDBW will try to
     * create a few statements as possible, but certain databases have restrictions that forces us to do the creation
     * in multiple steps.
     * @param schemaName Name of the schema to place the new table in, if null this will be the default schema
     *                   (according to the server) but some database server types ignores this parameter completely
     * @param name Name of the table
     * @param columns List of columns that the table should have; the order will be honoured
     * @param indexes List of indexes the table should have
     * @return An array of SQL that will create the table described
     */
    String[] getCreateTableStatement(String schemaName, String name, List<Column> columns, List<Index> indexes);

    /**
     * Generates an SQL statement that will drop the table specified
     * @param catalog Catalog the table is in, if null then it will not be part of the query
     * @param schema Schema the table is in, if null then it will not be part of the query
     * @param tableName Name of the table to drop, this cannot be null
     * @return SQL for the statement that will drop the table defined
     */
    String getDropTableStatement(String catalog, String schema, String tableName);

    /**
     * This method returns the character combination (usually # or --) that you should put in front of a line to comment
     * it out. Depending on database server implementation, the comment might work on partial lines, so that you can
     * put it on the same line as actual SQL code and so that everything that comes after will be inactive.
     * @return What to put in front of a line to comment it out
     */
    String getSingleLineCommentPrefix();
    
    /**
     * Some database server have a notion of a schema, which is an hierarchical unit under catalog. Some databases have
     * private user spaces in each catalog that are exposed in JDBC as schemas. This method will return what the name of
     * the "default" schema for this database server type is.
     * @return The name of the 'default' schema (PUBLIC in H2 and Postgres, dbo in Sybase, etc)
     */
    String getDefaultSchemaName();
    
    /**
     * This method will tell you if this database server can convert from one data type to another implicitly.
     * @param fromSqlType JDBC SQL type we want to convert from (constants out of java.sql.Types)
     * @param toSqlType JDBC SQL type we want to convert to (constants out of java.sql.Types)
     * @return True if this database can convert data of type fromSqlType to type toSqlType
     */
    boolean isCompatible(int fromSqlType, int toSqlType);
        
    /**
     * Attempts to convert a particular value to an acceptable format. Given a column on the server and the value we
     * want to put into this column, the method will try to convert the value into a format that is compatible with the
     * column.
     * @param targetColumnType Column we want to insert into
     * @param object Object we want to insert
     * @return A compatible value for the target column, that can be passed in as a parameter in a call on SQLExecutor
     */
    Object convertToCompatibleType(Column targetColumnType, Object object);
}
