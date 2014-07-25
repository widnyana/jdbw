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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    
    private final ServerMetaData metaDataResolver;
    private final Schema schema;
    private final String name;

    public View(ServerMetaData metaDataResolver, Schema schema, String name) {
        this.metaDataResolver = metaDataResolver;
        this.schema = schema;
        this.name = name;
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

    public List<ViewColumn> getColumns() throws SQLException {
        return metaDataResolver.getColumns(this);
    }

    public ViewColumn getColumn(String columnName) throws SQLException {
        for(ViewColumn column: metaDataResolver.getColumns(this)) {
            if(column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    public ViewColumn getColumn(int columnIndex) throws SQLException {
        return getColumns().get(columnIndex);
    }

    public Map<String, ViewColumn> getColumnMap() throws SQLException {
        Map<String, ViewColumn> columnMap = new TreeMap<String, ViewColumn>();
        for(ViewColumn column : getColumns()) {
            columnMap.put(column.getName(), column);
        }
        return new HashMap<String, ViewColumn>(columnMap);
    }

    /**
     * @return Number of columns this table has
     * @throws SQLException In an error occurred while reading information from
     * the database
     */
    public int getColumnCount() throws SQLException {
        return getColumns().size();
    }

    @Override
    public int compareTo(View o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return "View{" + getSchema().getCatalog().getName() + "." + getSchema().getName() + "." + getName() + "}";
    }
}
