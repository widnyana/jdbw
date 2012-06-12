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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mabe02
 */
public class StoredProcedure implements Comparable<StoredProcedure> {

    private final MetaDataResolver metaDataResolver;
    private final Catalog catalog;
    private final Schema schema;
    private final String name;
    private List<String> inputParameterNamesCache;

    public StoredProcedure(MetaDataResolver metaDataResolver, Catalog catalog, Schema schema, String name) {
        this.metaDataResolver = metaDataResolver;
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.inputParameterNamesCache = null;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public List<String> getInputParameterNames() throws SQLException {
        if(inputParameterNamesCache == null) {
            inputParameterNamesCache = metaDataResolver.getProcedureInputParameterNames(catalog.getName(), schema.getName(), this);
        }
        return Collections.unmodifiableList(inputParameterNamesCache);
    }

    @Override
    public int compareTo(StoredProcedure o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    public String getCode() throws SQLException {
        return metaDataResolver.getStoredProcedureCode(catalog.getName(), schema.getName(), name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
