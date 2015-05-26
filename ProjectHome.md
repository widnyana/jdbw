# JDBW #
JDBW is a thin wrapper layer around JDBC that will make database interactions a lot easier and more safe. If you just want to grab data from a database and don't want to care about all the database internals, with Statements and ResultSets, JDBW could make your life a lot easier. Think of JDBW as a helpful database utilities library that enables you to do the same things as with low-level JDBC, but with much fewer lines and less concern about closing resources. The motivation behind writing JDBW was to fill that spot where using Hibernate or similar high-level database libraries is too much overkill but using pure JDBC is too bothersome.

## Features ##
  * Designed around normal `javax.sql.DataSource`
    * Works easily with single connections too
  * Easier, automatically resource cleaning `DatabaseTransaction` and `AutoExecutor` classes
  * Super-simple `SQLWorker` that automates everything but the `SQLException` from you
  * Browse database meta data through simple classes (`Catalog`, `Schema`, `Table`, etc)
  * Supports multiple SQL dialects (more are easily added)
    * [MySQL](http://www.mysql.com)
    * [PostgreSQL](http://www.postgresql.org)
    * [Sybase ASE](http://www.sybase.com/products/databasemanagement/adaptiveserverenterprise)
    * [H2](http://www.h2database.com)
    * Generic JDBC
  * Only one dependency: slf4j-api
  * LGPL 3 licensed

## News ##
**2015-01-25**
  * Version 1.0.1 released!

**2014-07-27**
  * Version 1.0.0 released!
  * Updated documentation and added a quickstart

**2012-09-16**
  * Applied for Sonatype hosting, through [OSSRH-4319](https://issues.sonatype.org/browse/OSSRH-4319)
  * Finally updated the project summary here at Google Code

## Maven ##
To use JDBW with Maven, add this dependency to your `pom.xml`:
```
        <dependency>
            <groupId>com.googlecode.jdbw</groupId>
            <artifactId>jdbw</artifactId>
            <version>1.0.1</version>
        </dependency>
```

## Quickstart ##
For a more detailed guide on how to start using JDBW, please see [the developer guide in the Wiki section.](https://code.google.com/p/jdbw/wiki/DevelopmentGuide)

## Example ##
This example will connect to a MySQL server and print the server time:
```
//Create our MySQL server definition
MySQLServer mysqlServer = new MySQLServer("my-database.com", 3306, "mysql");

//Create the connection factory, which can be tuned with connection session variables and properties
MySQLDatabaseConnectionFactory connectionFactory = (MySQLDatabaseConnectionFactory)mysqlServer.newConnectionFactory();
connectionFactory.setUsername("my-db-user");
connectionFactory.setPassword("my-db-password");
connectionFactory.setUseCompression(true);

DatabaseConnection databaseConnection = null;
try {
    //Connect to the MySQL server using a single connection behind a DataSoure
    databaseConnection = connectionFactory.connect(new OneSharedConnectionDataSource.Factory());

    //Create a SQLWorker using an underlying auto-executor that will do all the work for us
    SQLWorker worker = new SQLWorker(databaseConnection.createAutoExecutor());

    //Ask the SQLWorker to run SELECT NOW() and give us back to top-left row from the result set and transform it to a String
    System.out.println(worker.topLeftValueAsString("SELECT NOW()"));
}
catch(SQLException e) {
    System.err.println(e.getMessage());
}
finally {
    //Finally close the database connection and the underlying connection
    if(databaseConnection != null)
        databaseConnection.close();
}
```