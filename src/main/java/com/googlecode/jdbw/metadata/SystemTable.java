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

/**
 * A system table is similar to a normal table, the JDBC driver may or may not decide that a particular table is a
 * system table or not. In general though, system tables are such that have a special meaning to the database and are
 * not used by applications.
 * @author Martin Berglund
 */
public class SystemTable extends Table {
    /**
     * Creates a SystemTable object from manually specified values
     * @param metaDataResolver Meta data resolver to be used when loading more properties of the table
     * @param schema Schema the table belongs to
     * @param tableName Name of the table
     */
    public SystemTable(ServerMetaData metaDataResolver, Schema schema, String tableName) {
        super(metaDataResolver, schema, tableName);
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Table", "SystemTable");
    }
}
