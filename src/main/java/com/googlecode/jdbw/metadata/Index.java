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

    public Index(Table table, String indexName, boolean unique, boolean clustered, boolean primaryKey, TableColumn... columns) {
        this.table = table;
        this.name = indexName;
        this.unique = unique;
        this.clustered = clustered;
        this.primaryKey = primaryKey;
        this.columns = new ArrayList<TableColumn>();
        this.columns.addAll(Arrays.asList(columns));
    }

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

    public boolean isClustered() {
        return clustered;
    }

    public int getNrOfColumns() {
        return columns.size();
    }

    public List<String> getColumnNames() {
        List<String> list = new ArrayList<String>(columns.size());
        for(Column column : columns) {
            list.add(column.getName());
        }
        return list;
    }

    public List<Column> getColumns() {
        return new ArrayList<Column>(columns);
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        return name + "(" + StringUtils.concatenateStringList(getColumnNames(), ", ") + ")";
    }
}
