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
package com.googlecode.jdbw.metadata;

import java.sql.SQLException;
import java.util.*;

/**
 * Table is the primary object you deal with in a database server, they contain
 * at least one column (often more) making the definition of the table and 0 or
 * more rows, which each row has a value for each column.
 * 
 * <p>A table normally has a <i>primary key</i>, a kind of unique index where each row
 * has a value that is unique for within this table. This primary key consists
 * of at least (and normally) one column but could be a combination of several.
 * If you look for a particular row in the table and you know the value of the
 * primary key column(s), this row can be fetched very quickly.
 * 
 * <p>Other than a primary key, a table may also have one or more <i>indexes</i>.
 * An index will make certain lookups much more efficient, you can find more
 * information about how this works in the description of the {@code Index} class.
 * 
 * @see Schema
 * @see Column
 * @see Index
 * @author Martin Berglund
 */
public class Table implements Comparable<Table> {

    private final ServerMetaData metaDataResolver;
    private final Schema schema;
    private final String name;
    private List<TableColumn> cachedColumns;
    private List<Index> cachedIndexes;

    /**
     * Creates a new table object based on a specified schema that it belongs to, a name and a meta-data resolving
     * object to be used when loading further details about this table
     * @param metaDataResolver
     * @param schema
     * @param tableName
     */
    public Table(ServerMetaData metaDataResolver, Schema schema, String tableName) {
        this.metaDataResolver = metaDataResolver;
        this.schema = schema;
        this.name = tableName;
        this.cachedColumns = null;
        this.cachedIndexes = null;
    }

    /**
     * Returns the name of the table this object is representing
     * @return Name of the table
     */
    public String getName() {
        return name;
    }

    /**
     * Return the schema that this table belongs to
     * @return The schema which owns the table
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * This method will return you the primary key if there is one, otherwise scan through the list of indexes and
     * return the first available unique index. If none is found, it returns {@code null}.
     * 
     * @return First available unique index or null
     * @throws SQLException In an error occurred while reading information from the database
     */
    public Index getUniqueKey() throws SQLException {
        if(getPrimaryKey() != null) {
            return getPrimaryKey();
        }
        for(Index index : getIndexes()) {
            if(index.isUnique()) {
                return index;
            }
        }
        return null;
    }

    /**
     * This method returns the primary key of the table, if there is one, otherwise it returns {@code null}.
     *
     * @return The primary key of this table, represented as an {@code Index} or {@code null} if there is no primary key
     * @throws SQLException In an error occurred while reading information from the database
     */
    public Index getPrimaryKey() throws SQLException {
        for(Index index : getIndexes()) {
            if(index.isPrimaryKey()) {
                return index;
            }
        }
        return null;
    }

    /**
     * Loads all the columns in this table, creates {@code Column} objects for them and returns those in a list order
     * in the same order as the columns appear in the table.
     * @return List of columns in the table, expected to be in order
     * @throws SQLException In an error occurred while reading information from the database
     */
    public List<TableColumn> getColumns() throws SQLException {
        List<TableColumn> cache = this.cachedColumns;
        if(cache == null) {
            cache = metaDataResolver.getColumns(this);
        }
        return cache;
    }

    /**
     * Loads all the indexes in this table, creates {@code Index} objects for them and returns those in a list. This
     * will include the primary key of the table, if there is one.
     * @return All indexes, including the primary key, on this table
     * @throws SQLException In an error occurred while reading information from the database
     */
    public List<Index> getIndexes() throws SQLException {
        List<Index> cache = this.cachedIndexes;
        if(cache == null) {
            cache = metaDataResolver.getIndexes(this);
        }
        return cache;
    }

    /**
     * Loads all the indexes, creates {@code Index} object for each one and then puts them into a map where the key is
     * the name of the index and the value is the index itself. This will include the primary key, if there is one.
     * @return Map (index name to {@code Index} object) of all indexes in this table, including the primary key
     * @throws SQLException In an error occurred while reading information from the database
     */
    public Map<String, Index> getIndexMap() throws SQLException {
        Map<String, Index> indexMap = new TreeMap<String, Index>();
        for(Index index : getIndexes()) {
            indexMap.put(index.getName(), index);
        }
        return new HashMap<String, Index>(indexMap);
    }

    /**
     * Loads one column in the table, specified by its name, and returns a {@code TableColumn} object representing it
     * @param columnName Name of the column to retrieve
     * @return {@code Column} representing the column or {@code null} if there was no column by this name in the table
     * @throws SQLException In an error occurred while reading information from the database
     */
    public TableColumn getColumn(String columnName) throws SQLException {
        for(TableColumn column: getColumns()) {
            if(column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * Loads one column in the table, specified by its index, and returns a {@code TableColumn} object representing it
     * @param columnIndex Index of the column (starting from 0)
     * @return Column representing the column at this index in the table
     * @throws SQLException In an error occurred while reading information from the database
     * @throws IndexOutOfBoundsException When the {@code columnIndex} is less than 0 or larger than the number of
     * columns in the table
     */
    public TableColumn getColumn(int columnIndex) throws SQLException {
        return getColumns().get(columnIndex);
    }

    /**
     * Loads all the columns, creates {@code TableColumn} object for each one and then puts them into a map where the
     * key is the name of the column and the value is the column itself.
     * @return Map (column name to {@code Column} object) of all columns in this table
     * @throws SQLException In an error occurred while reading information from the database
     */
    public Map<String, TableColumn> getColumnMap() throws SQLException {
        Map<String, TableColumn> columnMap = new TreeMap<String, TableColumn>();
        for(TableColumn column : getColumns()) {
            columnMap.put(column.getName(), column);
        }
        return new HashMap<String, TableColumn>(columnMap);
    }

    /**
     * Returns the number of columns in this table
     * @return Number of columns this table has
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public int getColumnCount() throws SQLException {
        return getColumns().size();
    }

    @Override
    public int compareTo(Table o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    /**
     * Flushes the cache for columns and indexes. These are loaded once and then kept in memory until this method is
     * called. Use it when you want to force a reload from the database.
     */
    public void clearCachedData() {
        cachedColumns = null;
        cachedIndexes = null;
    }
    
    @Override
    public String toString() {
        return "Table{" + getSchema().getCatalog().getName() + "." + getSchema().getName() + "." + getName() + "}";
    }
}
