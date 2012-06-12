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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mabe02
 */
public class Schema implements Comparable<Schema> {

    private final MetaDataResolver metaDataResolver;
    private final Catalog catalog;
    private final String name;

    public Schema(MetaDataResolver metaDataResolver, Catalog catalog, String name) {
        this.metaDataResolver = metaDataResolver;
        this.catalog = catalog;
        this.name = name;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public String getName() {
        return name;
    }

    public List<Table> getTables() throws SQLException {
        return getTableMetaData(null);
    }

    public Map<String, Table> getTableMap() throws SQLException {
        List<Table> tables = getTables();
        Map<String, Table> map = new HashMap<String, Table>();
        for(Table table : tables) {
            map.put(table.getName(), table);
        }
        return map;
    }

    public Table getTable(String tableName) throws SQLException {
        List<Table> list = getTableMetaData(tableName);
        if(list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private List<Table> getTableMetaData(String tableNameFilter) throws SQLException {
        List<String> userTableNames = metaDataResolver.getUserTableNames(catalog.getName(), name);
        List<Table> tables = new ArrayList<Table>(userTableNames.size());
        for(String tableName : userTableNames) {
            if(tableNameFilter != null && !tableNameFilter.equals(tableName)) {
                continue;
            }

            Table table = new Table(metaDataResolver, this, tableName);
            tables.add(table);
        }
        return tables;
    }

    public List<View> getViews() throws SQLException {
        return getViewMetaData(null);
    }

    private List<View> getViewMetaData(String viewNameFilter) throws SQLException {
        List<String> viewNames = metaDataResolver.getViewNames(catalog.getName(), name);
        List<View> views = new ArrayList<View>(viewNames.size());
        for(String viewName : viewNames) {
            if(viewNameFilter != null && !viewNameFilter.equals(viewName)) {
                continue;
            }

            View view = new View(catalog, this, viewName);
            views.add(view);
        }
        return views;
    }

    public List<StoredProcedure> getStoredProcedures() throws SQLException {
        return getStoredProceduresMetaData(null);
    }

    private List<StoredProcedure> getStoredProceduresMetaData(String procNameFilter) throws SQLException {
        List<String> procNames = metaDataResolver.getStoredProcedureNames(catalog.getName(), name);
        List<StoredProcedure> procs = new ArrayList<StoredProcedure>(procNames.size());
        for(String procName : procNames) {
            if(procNameFilter != null && !procNameFilter.equals(procName)) {
                continue;
            }

            StoredProcedure storedProcedure = new StoredProcedure(metaDataResolver, catalog, this, procName);
            procs.add(storedProcedure);
        }
        return procs;
    }

    @Override
    public int compareTo(Schema o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return getCatalog().getName() + "." + getName();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj instanceof Schema == false) {
            return false;
        }
        return toString().equals(obj.toString());
    }
}
