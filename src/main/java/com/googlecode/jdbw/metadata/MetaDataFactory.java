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
 * A factory interface to be implemented by database servers to customize how
 * and which meta data objects are constructed. This enables certain database
 * services to give out specialized subclasses of the common meta data objects
 * instead of the default implementation, if they need to override some 
 * particular behavior. 
 * @author Martin Berglund
 */
public interface MetaDataFactory {

    Catalog createCatalog(String catalogName);

    Schema createSchema(Catalog catalog, String schemaName);

    Table createTable(Schema schema, String tableName);
}
