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


/**
 * A default meta data factory implementation that will create default 
 * implementations of each meta data object ({@code Catalog}, {@code Table}, 
 * {@code Column}, etc...)
 * @author mabe02
 */
public class DefaultMetaDataFactory implements MetaDataFactory
{
    protected final MetaDataResolver metaDataResolver;

    protected DefaultMetaDataFactory(MetaDataResolver metaDataResolver)
    {
        this.metaDataResolver = metaDataResolver;
    }

    @Override
    public Catalog createCatalog(String catalogName)
    {
        return new Catalog(metaDataResolver, catalogName);
    }

    @Override
    public Schema createSchema(Catalog catalog, String schemaName)
    {
        return new Schema(metaDataResolver, catalog, schemaName);
    }

    @Override
    public Table createTable(Schema schema, String tableName)
    {
        return new Table(metaDataResolver, schema, tableName);
    }
}
