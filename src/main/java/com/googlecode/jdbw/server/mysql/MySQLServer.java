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

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.impl.DatabaseConnectionImpl;
import com.googlecode.jdbw.impl.SQLExecutorImpl;
import com.googlecode.jdbw.metadata.*;
import com.googlecode.jdbw.server.DatabaseServerTraits;
import com.googlecode.jdbw.server.StandardDatabaseServer;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author mabe02
 */
public class MySQLServer extends StandardDatabaseServer {

    public MySQLServer(String hostname, int port, String catalog, String username, String password) {
        this(new MySQLDefaultJDBCDriverDescriptor(), hostname, port, catalog, username, password);
    }

    public MySQLServer(JDBCDriverDescriptor driverDescriptor, String hostname, int port, String catalog, String username, String password) {
        super(driverDescriptor, hostname, port, catalog, username, password);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerType.MYSQL;
    }
    
    @Override
    public DatabaseServerTraits getServerTraits() {
        return new MySQLTraits();
    }

    @Override
    public Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", getUsername());
        properties.setProperty("password", getPassword());
        return properties;
    }

    @Override
    public MetaDataResolver createMetaDataResolver(DatabaseConnectionImpl connection) {
        return new MySQLMetaDataResolver(connection);
    }

    private static class MySQLMetaDataResolver extends MetaDataResolver {

        public MySQLMetaDataResolver(DatabaseConnectionImpl connectionPool) {
            super(connectionPool);
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

            if(indexName == null) //Only named indexes!
            {
                return;
            }

            if(indexMap.containsKey(indexName)) {
                indexMap.get(indexName).addColumn(column);
            } else {
                indexMap.put(indexName, new Index(indexName, unique, clustered, primaryKey, table, column));
            }
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

    private static class MySQLExecutor extends SQLExecutorImpl {

        public MySQLExecutor(Connection connection) {
            super(connection);
        }

        @Override
        protected PreparedStatement prepareExecuteStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        }

        @Override
        protected PreparedStatement prepareQueryStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        }

        @Override
        protected PreparedStatement prepareUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            return ps;
        }

        @Override
        protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
            return ps;
        }

        @Override
        protected void executeUpdate(Statement statement, String SQL) throws SQLException {
            super.executeUpdate(statement, SQL);
        }
    }

    public static class MySQLCatalog extends Catalog {

        public MySQLCatalog(MetaDataResolver metaDataResolver, String name) {
            super(metaDataResolver, name);
        }

        @Override
        public Schema getSchema(String schemaName) throws SQLException {
            return getSchemas().get(0);
        }
    }
}
