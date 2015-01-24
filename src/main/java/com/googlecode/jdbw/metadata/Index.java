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

import com.googlecode.jdbw.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An index on a database is a kind of fast lookup-table over (usually) one or
 * (sometimes) more columns in a database table. By adding an index, we can
 * make searching for rows matching particular criteria a lot faster, since the
 * lookup table can be used instead of looking through every row.
 * 
 * <p>This class represents such an index created on a database server. You 
 * normally don't create instances of this class yourself, but get them by 
 * asking a table to provide you with all the indexes that are created on this
 * table.
 * 
 * @see Table
 * @author Martin Berglund
 */
public class Index implements Comparable<Index> {

    private final String name;
    private final boolean unique;
    private final boolean clustered;
    private final boolean primaryKey;
    private final Table table;
    private final List<TableColumn> columns;

    /**
     * Creates an index object based on manually entered parameters
     * @param table Table the index belongs to
     * @param indexName Name of the index
     * @param unique Is this a unique index? Unique indexes prevents you from inserting two rows with identical values
     *               in the columns that are part of the index.
     * @param clustered Is this a clustered index? Clustered indexes normally means that the rows are stored physically
     *                  on disk as they are ordered in the index
     * @param primaryKey Is this index a primary key for the table?
     * @param columns List of columns that make up the index, needs to be at least one
     */
    public Index(Table table, String indexName, boolean unique, boolean clustered, boolean primaryKey, TableColumn... columns) {
        this.table = table;
        this.name = indexName;
        this.unique = unique;
        this.clustered = clustered;
        this.primaryKey = primaryKey;
        this.columns = new ArrayList<TableColumn>();
        this.columns.addAll(Arrays.asList(columns));
    }

    /**
     * Adds one more column to the index, at the end of the column list
     * @param column Column to add to the end of the column list
     */
    public void addColumn(TableColumn column) {
        if(column.getTable() != getTable()) {
            throw new IllegalArgumentException("Trying to add a column to an index from the wrong table!");
        }

        columns.add(column);
    }

    @Override
    public int compareTo(Index o) {
        if(primaryKey && !o.primaryKey) {
            return -1;
        }
        else if(!primaryKey && o.primaryKey) {
            return 1;
        }
        
        return name.compareTo(o.name);
    }

    /**
     * Returns {@code true} if this is a clustered indexes. Clustered indexes normally means that the rows are stored
     * physically on disk as they are ordered in the index
     * @return {@code true} if this index is clustered
     */
    public boolean isClustered() {
        return clustered;
    }

    /**
     * Returns the number of columns that are part of this index
     * @return Number of columns that are part of this index
     */
    public int getNrOfColumns() {
        return columns.size();
    }

    /**
     * Returns the name of all column that are part of this index
     * @return Name of all column that are part of this index
     */
    public List<String> getColumnNames() {
        List<String> list = new ArrayList<String>(columns.size());
        for(Column column : columns) {
            list.add(column.getName());
        }
        return list;
    }

    /**
     * Returns all column that are part of this index
     * @return All column that are part of this index
     */
    public List<Column> getColumns() {
        return new ArrayList<Column>(columns);
    }

    /**
     * Returns a particular column based on its position in the index
     * @param index Which column to get, where 0 is the first column in the index, 1 is the second and so on
     * @return Column which has the specified index
     * @throws java.lang.ArrayIndexOutOfBoundsException If the position index is smaller than 0 or larger than columns
     * in the index
     */
    public Column getColumn(int index) {
        return columns.get(index);
    }

    /**
     * Returns the name of this index
     * @return Name of this index
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the unique flag of this index. Unique indexes prevents you from inserting two rows with identical values
     *               in the columns that are part of the index.
     * @return {@code true} if this index is a unique index, {@code false} otherwise
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Returns {@code true} if this index is actually the primary key of the table it belongs to
     * @return {@code true} is this index is a primary key
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * Returns the table this index belongs to
     * @return Table this index belongs to
     */
    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        return "Index{" + name + "(" + StringUtils.concatenateStringList(getColumnNames(), ", ") + ")}";
    }
}
