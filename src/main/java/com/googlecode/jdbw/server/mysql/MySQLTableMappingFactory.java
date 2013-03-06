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
package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.objectstorage.Storable;
import com.googlecode.jdbw.objectstorage.TableMapping;
import com.googlecode.jdbw.objectstorage.TableMappingFactory;
import com.googlecode.jdbw.objectstorage.impl.DelegatingTableMappingFactory;

/**
 * Special table mapping factory for {@code MySQLJDBCObjectStorage} that decorates
 * the table mappings with a {@code MySQLTableMapping}.
 */
class MySQLTableMappingFactory extends DelegatingTableMappingFactory {

    MySQLTableMappingFactory(TableMappingFactory backend) {
        super(backend);
    }
    
    @Override
    public TableMapping createTableMapping(Class<? extends Storable> objectType) {
        return new MySQLTableMapping(super.createTableMapping(objectType));
    }
    
}
