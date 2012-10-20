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

import com.googlecode.jdbw.metadata.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned for MySQL
 * @author Martin Berglund
 */
class MySQLMetaDataResolver extends DefaultServerMetaData {

    MySQLMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Schema> getSchemas(Catalog catalog) throws SQLException {
        return Arrays.asList(createSchema(catalog, "schema"));
    }

    @Override
    public Schema getSchema(Catalog catalog, String schemaName) throws SQLException {
        if("schema".equals(schemaName)) {
            return getSchemas(catalog).get(0);
        }
        else {
            return null;
        }
    }
}
