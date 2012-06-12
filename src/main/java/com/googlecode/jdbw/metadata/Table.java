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
 * Copyright (C) 2007-2012 mabe02
 */
package com.googlecode.jdbw.metadata;

import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author mabe02
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

    public Catalog getCatalog() {
        return schema.getCatalog();
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

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

    public void invalidateCache() {
        cachedColumns = null;
        cachedIndexes = null;
    }

    public List<Column> getColumns() throws SQLException {
        if(cachedColumns != null) {
            return new ArrayList<Column>(cachedColumns);
        }

        cachedColumns = metaDataResolver.getColumns(schema.getCatalog().getName(), schema.getName(), this);
        return new ArrayList<Column>(cachedColumns);
    }

    public List<Index> getAllIndexes() throws SQLException {
        if(cachedIndexes != null) {
            return new ArrayList<Index>(cachedIndexes);
        }

        cachedIndexes = metaDataResolver.getIndexes(schema.getCatalog().getName(), schema.getName(), this);
        return new ArrayList<Index>(cachedIndexes);
    }

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

    public Column getColumn(int columnIndex) throws SQLException {
        if(cachedColumns == null) {
            getColumns();
        }
        return cachedColumns.get(columnIndex);
    }

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

    public Map<String, Column> getColumnMap() throws SQLException {
        Map<String, Column> columnMap = new TreeMap<String, Column>();
        for(Column column : getColumns()) {
            columnMap.put(column.getName(), column);
        }
        return new HashMap<String, Column>(columnMap);
    }

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
