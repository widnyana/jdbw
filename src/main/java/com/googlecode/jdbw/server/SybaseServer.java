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
package com.googlecode.jdbw.server;

import com.googlecode.jdbw.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author mabe02
 */
public class SybaseServer extends StandardDatabaseServer {

    private final String procName;

    private SybaseServer(String hostname, int port, String catalog, String username, String password, String procName) {
        super(hostname, port, catalog, username, password);
        this.procName = procName;
    }

    public static SybaseServer newInstance(String hostname, String catalog, String username, String password, String procName) {
        return newInstance(hostname, "5000", catalog, username, password, procName);
    }

    public static SybaseServer newInstance(String hostname, String port, String catalog, String username, String password, String procName) {
        return newInstance(hostname, new Integer(port), catalog, username, password, procName);
    }

    public static SybaseServer newInstance(String hostname, int port, String catalog, String username, String password, String procName) {
        return new SybaseServer(hostname, port, catalog, username, password, procName);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerType.SYBASE_ASE;
    }

    @Override
    protected Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("APPLICATIONNAME", procName);
        properties.setProperty("USER", getUsername());
        properties.setProperty("PASSWORD", getPassword());
        properties.setProperty("CHARSET", "utf8");
        return properties;
    }

    @Override
    protected String getJDBCUrl() {
        return "jdbc:sybase:Tds:" + getHostname() + ":" + getPort() + "/" + getCatalog();
    }

    @Override
    protected void loadDriver() {
        loadDriver("com.sybase.jdbc3.jdbc.SybDriver");
    }

    @Override
    public DatabaseServerTraits getServerTraits() {
        return new SybaseTraits();
    }

    @Override
    public String toString() {
        return "sybase://" + getHostname() + ":" + getPort() + "/" + getCatalog();
    }

    @Override
    protected DefaultDatabaseConnection createDatabaseConnection() {
        return new SybaseDatabaseConnectionPool(this);
    }

    public static class Factory extends DatabaseServerFactory {

        @Override
        public DatabaseServer createDatabaseServer(String hostname, int port, String catalog, String username, String password) {
            return createDatabaseServer(hostname, port, catalog, username, password, "JAVA");
        }

        public DatabaseServer createDatabaseServer(String hostname, int port, String catalog, String username, String password, String procName) {
            return new SybaseServer(hostname, port, catalog, username, password, procName);
        }
    }

    private static class SybaseDatabaseConnectionPool extends DefaultDatabaseConnection {

        public SybaseDatabaseConnectionPool(StandardDatabaseServer databaseServer) {
            super(databaseServer);
        }

        @Override
        protected MetaDataResolver createMetaDataResolver() {
            return new SybaseMetaDataResolver(this);
        }

        @Override
        protected PooledDatabaseConnection newPooledDatabaseConnection(Connection connection) {
            return new SybasePooledDatabaseConnection(connection);
        }
    }

    private static class SybasePooledDatabaseConnection extends PooledDatabaseConnection {

        public SybasePooledDatabaseConnection(Connection connection) {
            super(connection);
        }

        @Override
        protected SQLExecutor createExecutor() {
            return new SybaseSQLExecutor(this);
        }
    }

    private static class SybaseSQLExecutor extends DefaultSQLExecutor {

        public SybaseSQLExecutor(PooledDatabaseConnection pooledConnection) {
            super(pooledConnection);
        }

        @Override
        protected PreparedStatement prepareUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL);
            return ps;
        }

        @Override
        protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL);
            return ps;
        }

        @Override
        protected void executeUpdate(Statement statement, String SQL) throws SQLException {
            statement.executeUpdate(SQL);
        }

        @Override
        protected ResultSet getGeneratedKeys(Statement statement) throws SQLException {
            return new BogusResultSet();
        }
    }

    private static class SybaseMetaDataResolver extends DefaultMetaDataResolver {

        public SybaseMetaDataResolver(SybaseDatabaseConnectionPool connectionPool) {
            super(connectionPool);
        }

        @Override
        protected Column extractColumnFromMetaResult(ResultSet resultSet, Table table) throws SQLException {
            String columnName = resultSet.getString("COLUMN_NAME");
            int sqlType = resultSet.getInt("DATA_TYPE");
            String typeName = resultSet.getString("TYPE_NAME");
            int columnSize = resultSet.getInt("COLUMN_SIZE");
            int decimalDigits = resultSet.getInt("DECIMAL_DIGITS");
            int nullable = resultSet.getInt("NULLABLE");
            int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
            String isAutoIncrement = "";
            Column column = new Column(ordinalPosition, columnName, sqlType, typeName,
                    columnSize, decimalDigits, nullable, isAutoIncrement, table);
            return column;
        }

        @Override
        protected List<String> getStoredProcedureNames(String catalogName, String schemaName) throws SQLException {
            SQLWorker worker = new SQLWorker(connectionPool.createAutoExecutor());
            return worker.leftColumnAsString("SELECT name FROM " + catalogName + "..sysobjects WHERE type = 'P'");
        }

        @Override
        public String getStoredProcedureCode(String catalogName, String schemaName, String procedureName) throws SQLException {
            final StringBuilder sb = new StringBuilder();
            final AtomicInteger counter = new AtomicInteger(2);
            DatabaseTransaction transaction = connectionPool.beginTransaction(TransactionIsolation.READ_COMMITTED);
            transaction.execute(new ExecuteResultHandlerAdapter(), "use " + catalogName);
            transaction.execute(new ExecuteResultHandlerAdapter() {

                @Override
                public boolean nextResultSet() {
                    counter.decrementAndGet();
                    return true;
                }

                @Override
                public boolean nextRow(Object[] row) {
                    if(counter.get() > 0) {
                        return true;
                    }

                    sb.append((String) row[0]);
                    return true;
                }
            }, "sp_helptext " + procedureName);
            transaction.execute(new ExecuteResultHandlerAdapter(), "use " + connectionPool.getDefaultCatalogName());
            transaction.rollback();
            return sb.toString();
        }

        @Override
        protected List<Index> getIndexes(String catalogName, String schemaName, Table table) throws SQLException {
            List<Index> indexes = super.getIndexes(catalogName, schemaName, table);
            List<Index> newIndexList = new ArrayList<Index>(indexes.size());
            SQLWorker worker = new SQLWorker(connectionPool.createAutoExecutor());

            List<Object[]> rows = worker.query("select i.name, i.status, i.status2 "
                    + "FROM " + catalogName + "." + schemaName + ".sysobjects o, "
                    + "     " + catalogName + "." + schemaName + ".sysindexes i "
                    + "WHERE o.name = ? AND o.type = 'U' AND "
                    + "         o.id = i.id", table.getName());

            //Use a safer way of detecting clustered indexes
            indexLoop:
            for(Index index : indexes) {
                for(Object[] row : rows) {
                    if(row[0].toString().trim().equals(index.getName())) {
                        int status = (Integer) row[1];
                        int status2 = (Integer) row[2];
                        boolean clustered = (status & 0x10) > 0 || (status2 & 0x0200) > 0;
                        Index newIndex = new Index(
                                index.getName(), index.isUnique(), clustered,
                                index.isUnique() && clustered, table, index.getColumns().get(0));

                        for(int i = 1; i < index.getColumnNames().size(); i++) {
                            newIndex.addColumn(index.getColumns().get(i));
                        }

                        newIndexList.add(newIndex);
                        continue indexLoop;
                    }
                }

                newIndexList.add(index);
            }
            return newIndexList;
        }
    }
}
