# Create the MongoManager

First of all, you need to connect to the mongo database. Do that by creating a new instance of `MongoManager`.

_Example of creating a `MongoManager`:_

```java
public class Application {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
    }
}
```

If a `MongoManager` is created without arguments, the credentials are read from the following locations:

1. From Disk: `{applicationDirectory}/credentials.properties`
2. From Resource: `{applicationJar}/credentials.properties`

If no credentials are found, an exception is thrown.

Here you can see the default keys of the two required properties.

_Default `credentials.properties`:_

```properties
en2do.connectString=mongodb://<username>:<password>@<host>:<port>/?<options>
en2do.database=<database>
```

You can also create credentials through various other methods.

_Example of creating new `Credentials`:_

```java
public class Application {
    public static void main(String[] args) {
        Credentials credentials;

        // Loading properties from resources file /resources/credentials.properties
        credentials = Credentials.fromResource(); 
        // Loading properties erty from resources file of given path
        credentials = Credentials.fromResource("/path/to/resources.properties");
        
        // Loading properties from the file-storage {jar-directory}/credentials.properties
        credentials = Credentials.fromFile();
        // Loading properties from the file-storage of given path
        credentials = Credentials.fromFile("/path/to/file.properties");
        
        // Loading the properties from the system properties
        credentials = Credentials.fromSystemProperties();
        // Loading the properties from the system properties, 
        // using custom keys
        credentials = Credentials.fromSystemProperties("CONNECT_KEY", "DATABASE_KEY");

        // Keys are "EN2DO_CONNECTSTRING" and "EN2DO_DATABASE"
        
        // Loading the properties from the system environmental variables
        credentials = Credentials.fromSystemEnvVars();
        // Loading the properties from the system environmental variables, 
        // using custom keys
        credentials = Credentials.fromSystemEnvVars("CONNECT_KEY", "DATABASE_KEY");

        // Creating credentials with strings.
        credentials = Credentials.of("connectString", "database");
    }
}
```

After you created a `Credentials` object, just pass it into the `MongoManager` constructor.

_Example of creating a new `MongoManager` with `Credentials`:_

```java
public class Application {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager(Credentials.of("connectString", "databaseName"));
    }
}
```

[Learn more about the MongoDB ConnectionString](https://www.mongodb.com/docs/manual/reference/connection-string/)
