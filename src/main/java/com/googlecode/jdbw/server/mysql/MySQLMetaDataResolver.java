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
 * Copyright (C) 2009-2012 mabe02
 */

package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.metadata.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author mabe02
 */
class MySQLMetaDataResolver extends MetaDataResolver {

    public MySQLMetaDataResolver(DataSource dataSource) {
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
    protected void extractIndexDataFromMetaResult(ResultSet resultSet, Map<String, Index> indexMap, Table table) throws SQLException {
        String indexName = resultSet.getString("INDEX_NAME");
        boolean unique = !resultSet.getBoolean("NON_UNIQUE");
        boolean clustered = resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexClustered;
        String columnName = resultSet.getString("COLUMN_NAME");
        boolean primaryKey = "PRIMARY".equals(indexName);
        Column column = table.getColumn(columnName);
        if(indexName == null) {
            return;
        }
        if(indexMap.containsKey(indexName)) {
            indexMap.get(indexName).addColumn(column);
        } else {
            indexMap.put(indexName, new Index(indexName, unique, clustered, primaryKey, table, column));
        }
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
