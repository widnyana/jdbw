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
package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.metadata.Catalog;
import com.googlecode.jdbw.metadata.DefaultServerMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned to H2
 * @author Martin Berglund
 */
public class H2MetaDataResolver extends DefaultServerMetaData {

    H2MetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    protected ResultSet getSchemaMetadata(Connection pooledConnection, Catalog catalog, String schemaName) throws SQLException {
        return pooledConnection.getMetaData().getSchemas();
    }
}
