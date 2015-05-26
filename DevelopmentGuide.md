# Getting the library #
Google code doesn't provide any file hosting any more, so you'll either need to use [Maven](Maven.md) for your project or download the jar file from [Maven Central](http://search.maven.org/#search|ga|1|a%3A%22jdbw%22).

# Getting the JDBC driver #
JDBW is just a wrapper around JDBC, it doesn't actually contain any JDBC drivers and is not able to make an actual database connection on its own. Even the Maven artifact doesn't bring in any jdbc drivers. So before using JDBW, make sure you have the desired JDBC driver(s) available on the classpath.

# Coding #
## Step 1: Define your database server ##
The first thing you need to do is describe your database server you want to connect to. You do this by creating an instance of an appropriate implementation of the `DatabaseServer` interface. JDBW comes with these ones:
  * `H2FileBasedServer`
  * `H2InMemoryServer`
  * `H2NetworkServer`
  * `MySQLServer`
  * `PostgreSQLServer`
  * `SybaseASEServer`

In our example, let's define a PostreSQL server:
```
PostgreSQLServer postgreSQLServer = new PostgreSQLServer("my-db-server.mydomain.com", 5432, "catalog1");
```
This will declare that there is a PostgreSQL server running on hostname "my-db-server" (on default port 5432) with a catalog "catalog1"

## Step 2: Create a connection factory ##
In order to establish a connection to this database, we'll need a `DatabaseConnectionFactory`. This object is used for setting connection-related properties such as username and password to use, but also any other property that the JDBC driver supports.
```
DatabaseConnectionFactory databaseConnectionFactory = postgreSQLServer.newConnectionFactory();
((AuthenticatingDatabaseConnectionFactory)databaseConnectionFactory).setUsername("my-user");
((AuthenticatingDatabaseConnectionFactory)databaseConnectionFactory).setPassword("my-password");
```
In the sample code above, we're asking the `postgreSQLServer` object to create a connection factory for us. Note that the `DatabaseConnectionFactory` interface returned doesn't contain much, so we'll type-cast it to `AuthenticatingDatabaseConnectionFactory` (which all database servers that supports user authentication will use) in order to set the username and password.

## Step 3: Establish the connection ##
Now we want to get our database connection up and established, what do we need to do? Well, first of all, whenever you use databases programmatically, you probably want to use connection pooling. Connection pool implementations are difficult to write and there are some widely used ones available, so JDBW doesn't try to re-invent the wheel. It will let you use your favourite connection pooling library by using a `javax.sql.DataSource` to allocate its connections:
```
DatabaseConnection databaseConnection = databaseConnectionFactory.connect(new DataSourceFactory() {
    @Override
    public DataSource newDataSource(String jdbcUrl, Properties properties) {
        //Create a DataSource here with your favorite connection pool library
    }

    @Override
    public void close(DataSource previouslyConstructedDataSource) {
        //Chech your connection pool library on how to close previously create DataSource:s
    }
});
```
If you don't want to use any connection pooling, there's a dummy implementation called `OneSharedConnectionDataSource` shipped with JDBW that you can use:
```
DatabaseConnection databaseConnection = databaseConnectionFactory.connect(new OneSharedConnectionDataSource.Factory());
```

## Step 4a: Query the database using an executor ##
JDBW gives you two main ways of querying the database (although under the hood they are the same), through `SQLExecutor` and through `SQLWorker`. The `SQLExecutor` in turn has two flavours, either an `AutoExecutor` or a `DatabaseTransaction`. The differences between these to flavours are that with an `AutoExecutor`, each query will allocate a connection from the underlying `DataSource`, set it to auto-commit mode, run the query and then return the connection to the pool. A `DatabaseTransaction` will allocate a connection on creation but won't return it unless you call either `.commit()` or `.rollback()` on it. Because of this, `AutoExecutor` is safer in terms of starving the pool. Using the `SQLExecutor` interface can be a bit verbose though:
```
databaseConnection.createAutoExecutor().execute(new ExecuteResultHandler() {
    @Override
    public boolean onResultSet(ResultSetInformation information) {
        //This is called one on every result set, just before rows are processed. Return true if you want to 
        //process rows from this result set and false if you want to discard it
    }

    @Override
    public boolean nextRow(Object[] row) {
        //This is called for each row in the result set, return true if you want JDBW to continue working 
        //through the result set and false if you are done with it
    }

    @Override
    public void onUpdateCount(int updateCount) {
        //JDBW will pass the update-count into here, if supported by the driver for this query type
    }

    @Override
    public void onGeneratedKey(Object object) {
        //If the query generated any key (auto-incrementing ids?), it will be passed in here
    }

    @Override
    public void onWarning(SQLWarning warning) {
        //If the server sent any SQLWarning:s, JDBW will invoke this method, one at a time
    }

    @Override
    public void onDone() {
        //This method is called by JDBW after the query is done, just before returning from the .execute() call
    }
}, "SELECT * FROM Orders WHERE CustomerId = ?", customerId);
```
There is an adapter implementation that will make processing queries a little bit easier:
```
databaseConnection.createAutoExecutor().execute(new ExecuteResultHandlerAdapter() {
        @Override
        public boolean nextRow(Object[] row) {
            //This is called for each row in the result set, return true if you want JDBW to continue working
            //through the result set and false if you are done with it
        }
    }, "SELECT * FROM Orders WHERE CustomerId = ?", customerId);
```
Note that you can use normal JDBC parameter substitution by using "?" in the SQL query.

## Step 4b: Query the database using an SQL worker ##
In addition to the somewhat complicated `SQLExecutor` interface, there's a much easier utility class that will make 99% of your queries much easier to work with:
```
SQLWorker worker = new SQLWorker(databaseConnection.createAutoExecutor());
for(String row[]: worker.queryAsStrings("SELECT CustomerId, Quantity FROM Orders WHERE CustomerId NOT IN (?, ?)", 616, 666)) {
    System.out.print("Customer ID: " + row[0]);
    System.out.println(", order quantity: " + row[1]);
}
```

The `SQLWorker` class has many useful methods for working easily with many common type of result sets, loading and converting it to simpler data structures. It works with both `AutoExecutor` and `DatabaseTransaction` but if you use it with the latter, be sure to note that it doesn't call commit, you'll still need to do that.

## Step 5: Closing the connection ##
When you are done with your `DatabaseConnection`, closing it is easy:
```
databaseConnection.close();
```
This will close your underlying `DataSource` for you. You can also close the underlying `DataSource` directly and let the `DatabaseConnection` be GC:ed.