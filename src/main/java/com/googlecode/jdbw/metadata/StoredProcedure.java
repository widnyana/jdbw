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
 * @author Martin Berglund
 */
public class StoredProcedure implements Comparable<StoredProcedure> {

    private final ServerMetaData metaDataResolver;
    private final Schema schema;
    private final String name;

    public StoredProcedure(ServerMetaData metaDataResolver, Schema schema, String name) {
        this.metaDataResolver = metaDataResolver;
        this.schema = schema;
        this.name = name;
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

    @Override
    public int compareTo(StoredProcedure o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return schema.getCatalog().getName() + "." + schema.getName() + "." + getName();
    }
}
