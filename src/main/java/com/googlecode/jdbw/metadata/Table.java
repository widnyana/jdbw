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

    private final MetaDataResolver metaDataResolver;
    private final Schema schema;
    private final String name;
    private List<Index> cachedIndexes;
    private List<Column> cachedColumns;

    public Table(MetaDataResolver metaDataResolver, Schema schema, String tableName) {
        this.metaDataResolver = metaDataResolver;
        this.schema = schema;
        this.name = tableName;
        this.cachedIndexes = null;
        this.cachedColumns = null;
    }

    /**
     * @return The catalog which this table sorts under, i.e. the owner of this
     * table's schema
     */
    public Catalog getCatalog() {
        return schema.getCatalog();
    }

    /**
     * @return Name of the table
     */
    public String getName() {
        return name;
    }

    /**
     * @return The schema which owns the table
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * This method will return you the primary key if there is one, otherwise
     * scan through the list of indexes and return the first available unique
     * index. If none is found, it returns null.
     * 
     * @return First available unique index or null
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Index getUniqueKey() throws SQLException {
        if(getPrimaryKey() != null) {
            return getPrimaryKey();
        }

        for(Index index : getAllIndexes()) {
            if(index.isUnique()) {
                return index;
            }
        }

        return null;
    }

    /**
     * @return The primary key of this table, represented as an {@code Index}
     * or {@code null} if there is no primary key
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Index getPrimaryKey() throws SQLException {
        if(cachedIndexes == null) {
            getAllIndexes();
        }
        for(Index index : cachedIndexes) {
            if(index.isPrimaryKey()) {
                return index;
            }
        }
        return null;
    }

    /**
     * This table class will cache the columns and indexes after reading them
     * from the database server once, this method will clear the cache, forcing
     * them to be re-read. 
     */
    public void invalidateCache() {
        cachedColumns = null;
        cachedIndexes = null;
    }

    /**
     * @return List of columns in the table, expected to be in order
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public List<Column> getColumns() throws SQLException {
        if(cachedColumns != null) {
            return new ArrayList<Column>(cachedColumns);
        }

        cachedColumns = metaDataResolver.getColumns(schema.getCatalog().getName(), schema.getName(), this);
        return new ArrayList<Column>(cachedColumns);
    }

    /**
     * @return All indexes, including the primary key, on this table
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public List<Index> getAllIndexes() throws SQLException {
        if(cachedIndexes != null) {
            return new ArrayList<Index>(cachedIndexes);
        }

        cachedIndexes = metaDataResolver.getIndexes(schema.getCatalog().getName(), schema.getName(), this);
        return new ArrayList<Index>(cachedIndexes);
    }

    /**
     * Looks up one column
     * @param columnName Name of the column to retrieve
     * @return {@code Column} representing the column or {@code null} if there
     * was no column by this name in the table
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Column getColumn(String columnName) throws SQLException {
        if(cachedColumns == null) {
            getColumns();
        }
        for(Column column : cachedColumns) {
            if(column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * Retrieves a column by index
     * the database
     * @param columnIndex Index of the column (starting from 0)
     * @return Column representing the column at this index in the table
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Column getColumn(int columnIndex) throws SQLException {
        if(cachedColumns == null) {
            getColumns();
        }
        return cachedColumns.get(columnIndex);
    }

    /**
     * @return Number of columns this table has
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public int getNrOfColumns() throws SQLException {
        if(cachedColumns == null) {
            getColumns();
        }
        return cachedColumns.size();
    }

    @Override
    public int compareTo(Table o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    /**
     * @return Map (column name to {@code Column} object) of all columns in this
     * table
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Map<String, Column> getColumnMap() throws SQLException {
        Map<String, Column> columnMap = new TreeMap<String, Column>();
        for(Column column : getColumns()) {
            columnMap.put(column.getName(), column);
        }
        return new HashMap<String, Column>(columnMap);
    }

    /**
     * @return Map (index name to {@code Index} object) of all indexes in this
     * table, including the primary key
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public Map<String, Index> getIndexMap() throws SQLException {
        Map<String, Index> indexMap = new TreeMap<String, Index>();
        for(Index index : getAllIndexes()) {
            indexMap.put(index.getName(), index);
        }
        return new HashMap<String, Index>(indexMap);
    }

    @Override
    public String toString() {
        return getSchema().getCatalog().getName() + "." + getSchema().getName() + "." + getName();
    }
}
