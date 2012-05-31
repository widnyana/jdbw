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
package com.googlecode.jdbw.metadata;

import com.googlecode.jdbw.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mabe02
 */
public class Index implements Comparable<Index> {

    private final String name;
    private final boolean unique;
    private final boolean clustered;
    private final boolean primaryKey;
    private final Table table;
    private final List<Column> columns;

    public Index(String indexName, boolean unique, boolean clustered, boolean primaryKey, Table table, Column firstColumn) {
        this.name = indexName;
        this.unique = unique;
        this.clustered = clustered;
        this.primaryKey = primaryKey;
        this.table = table;
        this.columns = new ArrayList<Column>();
        this.columns.add(firstColumn);
    }

    public void addColumn(Column column) {
        if(column.getTable() != getTable()) {
            throw new IllegalArgumentException("Trying to add a column to an index from the wrong table!");
        }

        columns.add(column);
    }

    @Override
    public int compareTo(Index o) {
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
