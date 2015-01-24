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
import java.util.List;

/**
 * A database catalog, sometimes known as a "database" on the database server. This is normally the highest level of
 * organization in the database, where each data domain is assigned a distinct catalog. A catalog will in turn consist
 * of one or more <i>schemas</i> that divides the objects further.
 * 
 * <p/>Some database servers only supports one catalog. If the concept of catalog doesn't exist in the terminology of
 * the server you are using, JDBW can be expected to create a "dummy" catalog object for you.
 * 
 * @see Schema
 * @author Martin Berglund
 */
public class Catalog implements Comparable<Catalog> {

    private final ServerMetaData metaDataResolver;
    private final String name;

    /**
     * Creates a new object representing a database catalog with a particular name and a meta-data resolver object to
     * use when going deeper in the hierarchy
     * @param metaDataResolver Meta-data object to use when loading the content of this catalog
     * @param name Name of the catalog we are representing
     */
    public Catalog(ServerMetaData metaDataResolver, String name) {
        this.metaDataResolver = metaDataResolver;
        this.name = name;
    }

    /**
     * Name of the catalog
     * @return Name of this catalog
     */
    public String getName() {
        return name;
    }

    /**
     * Schemas are the level below catalogs in order of organization, as exposed by JDBC. This method returns all
     * schemas that are under the catalog this object represents.
     * @return List of schemas under this catalog
     * @throws SQLException If there was a database error when loading schemas
     */
    public List<Schema> getSchemas() throws SQLException {
        return metaDataResolver.getSchemas(this);
    }

    /**
     * Returns a particular Schema that lies under this catalog, based on name
     * @param schemaName Name of the schema we want to retrieve
     * @return A Schema object representing the schema specified, or {@code null} if no such schema existed under this
     * catalog
     * @throws SQLException If there was a database error when loading schema information
     */
    public Schema getSchema(String schemaName) throws SQLException {
        return metaDataResolver.getSchema(this, schemaName);
    }

    @Override
    public int compareTo(Catalog o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return "Catalog{" + getName() + "}";
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Catalog)) {
            return false;
        }

        Catalog other = (Catalog) obj;
        return metaDataResolver == other.metaDataResolver
                && getName().equals(other.getName());
    }
}
