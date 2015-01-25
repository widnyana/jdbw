package com.googlecode.jdbw.objectstorage;

import com.googlecode.jdbw.*;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;
import com.googlecode.jdbw.server.mysql.MySQLDatabaseConnectionFactory;
import com.googlecode.jdbw.server.mysql.MySQLServer;
import com.googlecode.jdbw.server.postgresql.PostgreSQLServer;
import com.googlecode.jdbw.util.ExecuteResultHandlerAdapter;
import com.googlecode.jdbw.util.OneSharedConnectionDataSource;
import com.googlecode.jdbw.util.SQLWorker;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;

/**
 * Sample implementation to demonstrate how to use JDBW
 */
public class Sample {
    public static void main(String[] args) {
        PostgreSQLServer postgreSQLServer = new PostgreSQLServer("my-db-server.mydomain.com", 5432, "catalog1");
        DatabaseConnectionFactory databaseConnectionFactory = postgreSQLServer.newConnectionFactory();
        ((AuthenticatingDatabaseConnectionFactory)databaseConnectionFactory).setUsername("my-user");
        ((AuthenticatingDatabaseConnectionFactory)databaseConnectionFactory).setPassword("my-password");

        DatabaseConnection databaseConnection = databaseConnectionFactory.connect(new OneSharedConnectionDataSource.Factory());

        databaseConnection.close();
    }
}
