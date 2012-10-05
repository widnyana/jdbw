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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned for MySQL
 * @author Martin Berglund
 */
class MySQLMetaDataResolver extends MetaDataResolver {

    MySQLMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected MetaDataFactory getMetaDataFactory() {
        return new MySQLMetaDataFactory(this);
    }

    @Override
    protected List<String> getSchemaNames(String catalogName) throws SQLException {
        return Arrays.asList("schema");
    }

    @Override
    protected List<String> getProcedureInputParameterNames(String catalogName, String schemaName, StoredProcedure procedure) throws SQLException {
        return super.getProcedureInputParameterNames(catalogName, null, procedure);
    }

    @Override
    protected Map<String, Object> extractIndexDataFromMetaResult(ResultSet resultSet) throws SQLException {
        Map<String, Object> indexDefMap = new HashMap<String, Object>();
        indexDefMap.put("INDEX_NAME", resultSet.getString("INDEX_NAME"));
        indexDefMap.put("NON_UNIQUE", resultSet.getBoolean("NON_UNIQUE"));
        indexDefMap.put("TYPE", resultSet.getShort("TYPE"));
        indexDefMap.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
        if(indexDefMap.get("INDEX_NAME").equals("PRIMARY")) {
            indexDefMap.put("TYPE", DatabaseMetaData.tableIndexClustered);
        }
        return indexDefMap;
    }

    private static class MySQLMetaDataFactory extends DefaultMetaDataFactory {

        public MySQLMetaDataFactory(MetaDataResolver metaDataResolver) {
            super(metaDataResolver);
        }

        @Override
        public Catalog createCatalog(String catalogName) {
            return new MySQLCatalog(metaDataResolver, catalogName);
        }
    }
    
    private static class MySQLCatalog extends Catalog {

        public MySQLCatalog(MetaDataResolver metaDataResolver, String name) {
            super(metaDataResolver, name);
        }

        @Override
        public Schema getSchema(String schemaName) throws SQLException {
            return getSchemas().get(0);
        }
    }
}
