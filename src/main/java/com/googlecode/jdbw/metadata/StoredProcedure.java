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
 * A <i>stored procedure</i> is normally a piece of programming code that has 
 * been pre-created on the server and has been compiled into some intermediate
 * format, to be executed by a specific command. Normally this code is some kind 
 * of imperative SQL-like language, but could really be anything.
 * 
 * <p>A stored procedure may or may not have input parameters and some database
 * servers also supports output parameters to give data back to the caller.
 * 
 * <p>You normally won't create instances of this class yourself, but rather
 * will be supplied with them by asking a {@code Schema} to give you the list 
 * of stored procedures it has.
 * 
 * @see Schema
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

    /**
     * @return Name of the stored procedure
     */
    public String getName() {
        return name;
    }

    /**
     * @return Schema that owns this stored procedure
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @return Catalog this stored procedure is sorted under, i.e. the owner
     * of this stored procedure's schema
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * @return List of input parameter names, in the order the stored procedure
     * expects them
     * @throws SQLException If an error occurred while reading the information
     * from the database
     */
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
