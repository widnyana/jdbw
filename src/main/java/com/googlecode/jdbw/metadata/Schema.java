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

/**
 * In database terminology, especially in the JDBC world, a <i>Schema</i> is a
 * middle level organizational container. Owner by a <i>Catalog</i>, a Schema
 * will be the owner of tables, system tables, stored procedures, function and
 * views. A catalog may contain one or more schemas, but it's not uncommon among
 * database servers to provide only one per catalog by default (you have to 
 * create more yourself), normally called <i>PUBLIC</i>.
 * 
 * @author Martin Berglund
 */
public class Schema implements Comparable<Schema> {

    private final ServerMetaData metaDataResolver;
    private final Catalog catalog;
    private final String name;

    public Schema(ServerMetaData metaDataResolver, Catalog catalog, String name) {
        this.metaDataResolver = metaDataResolver;
        this.catalog = catalog;
        this.name = name;
    }

    /**
     * @return Catalog owning this schema
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * @return Name of the schema
     */
    public String getName() {
        return name;
    }

    /**
     * @return List of all tables in this schema
     * @throws SQLException If an error occurred while reading the list of tables
     */
    public List<Table> getTables() throws SQLException {
        return metaDataResolver.getTables(this);
    }

    /**
     * @return Map (table name to {@code Table} object) of all tables in this
     * schema
     * @throws SQLException If an error occurred while reading the list of tables
     */
    public Map<String, Table> getTableMap() throws SQLException {
        List<Table> tables = getTables();
        Map<String, Table> map = new HashMap<String, Table>();
        for(Table table : tables) {
            map.put(table.getName(), table);
        }
        return map;
    }

    /**
     * Creates and returns a {@code Table} object for a particular table.
     * 
     * @param tableName Name of the table to get the {@code Table} object for
     * @return {@code Table} representing the table or null if there was no
     * table in the schema with this name
     * @throws SQLException If an error occurred while reading the list of tables
     */
    public Table getTable(String tableName) throws SQLException {
        return metaDataResolver.getTable(this, tableName);
    }

    /**
     * @return List of all views in this schema
     * @throws SQLException If an error occurred while reading the list of views
     */
    public List<View> getViews() throws SQLException {
        return metaDataResolver.getViews(this);
    }

    /**
     * @return List of all views in this schema
     * @throws SQLException If an error occurred while reading the list of views
     */
    public View getView(String viewName) throws SQLException {
        return metaDataResolver.getView(this, viewName);
    }

    /**
     * @return List of all stored procedures in this schema
     * @throws SQLException If an error occurred while reading the list of stored
     * procedures
     */
    public List<StoredProcedure> getStoredProcedures() throws SQLException {
        return metaDataResolver.getStoredProcedures(this);
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
