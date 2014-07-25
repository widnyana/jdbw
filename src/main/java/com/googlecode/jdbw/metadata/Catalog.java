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
 * A database catalog, sometimes known as a "database" on the database server.
 * This is normally the highest level of organization in the database, where
 * each data domain is assigned a distinct catalog. A catalog will in turn
 * consist of one or more <i>schemas</i> that divides the objects further.
 * 
 * <p>Some database servers only supports one catalog. If the concept of catalog
 * doesn't exist in the terminology of the server you are using, JDBW can be
 * expected to create a "dummy" catalog object for you.
 * 
 * @see Schema
 * @author Martin Berglund
 */
public class Catalog implements Comparable<Catalog> {

    private final ServerMetaData metaDataResolver;
    private final String name;

    public Catalog(ServerMetaData metaDataResolver, String name) {
        this.metaDataResolver = metaDataResolver;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Schema> getSchemas() throws SQLException {
        return metaDataResolver.getSchemas(this);
    }

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
