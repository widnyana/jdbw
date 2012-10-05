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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A <i>View</i> in the database world is a kind of virtual table, defined by
 * a SELECT statement that is being run every time you select from the view.
 * Using views, you can make very complicated data models easier to read and
 * access, providing joins and restrictions under the cover, so that users not
 * familiar with the data model can still use it.
 * 
 * @see Schema
 * @author Martin Berglund
 */
public class View implements Comparable<View> {
    
    private final MetaDataResolver metaDataResolver;
    private final Schema schema;
    private final String name;
    private List<Index> cachedIndexes;
    private List<ViewColumn> cachedColumns;

    public View(MetaDataResolver metaDataResolver, Schema schema, String name) {
        this.metaDataResolver = metaDataResolver;
        this.schema = schema;
        this.name = name;
        this.cachedIndexes = null;
        this.cachedColumns = null;
    }

    /**
     * @return Name of the view
     */
    public String getName() {
        return name;
    }

    /**
     * @return Schema owning this view
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @return Catalog this view is sorted under, i.e. the owner of the view's 
     * Schema.
     */
    public Catalog getCatalog() {
        return schema.getCatalog();
    }

    @Override
    public int compareTo(View o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return getSchema().getCatalog().getName() + "." + getSchema().getName() + "." + getName();
    }

    /**
     * This view class will cache the columns and indexes after reading them
     * from the database server once, this method will clear the cache, forcing
     * them to be re-read. 
     */
    public void invalidateCache() {
        cachedColumns = null;
        cachedIndexes = null;
    }

    public List<ViewColumn> getColumns() throws SQLException {
        if(cachedColumns != null) {
            return new ArrayList<ViewColumn>(cachedColumns);
        }

        List<Map<String, Object>> columnMaps = metaDataResolver.getColumns(schema.getCatalog().getName(), schema.getName(), getName());
        List<ViewColumn> columns = new ArrayList<ViewColumn>();
        for(Map<String, Object> columnMap: columnMaps) {
            String columnName = (String)columnMap.get("COLUMN_NAME");
            int sqlType = (Integer)columnMap.get("DATA_TYPE");
            String typeName = (String)columnMap.get("TYPE_NAME");
            int columnSize = (Integer)columnMap.get("COLUMN_SIZE");
            int decimalDigits = (Integer)columnMap.get("DECIMAL_DIGITS");
            int nullable = (Integer)columnMap.get("NULLABLE");
            int ordinalPosition = (Integer)columnMap.get("ORDINAL_POSITION");
            String isAutoIncrement = (String)columnMap.get("IS_AUTOINCREMENT");
            columns.add(new ViewColumn(
                    this,
                    ordinalPosition, 
                    columnName, 
                    sqlType, 
                    typeName, 
                    columnSize, 
                    decimalDigits, 
                    nullable, 
                    isAutoIncrement));
        }
        cachedColumns = columns;
        return new ArrayList<ViewColumn>(cachedColumns);
    }
}
