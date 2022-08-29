# _// en2do_

Sync/Async entity framework for mongodb in Java 17

## Overview
* Tests
  * [TestUnits](src/test/java/eu/koboo/en2do/test/cases)
  * [TestEntity](src/test/java/eu/koboo/en2do/test/customer)
* Get Started
  * [Add as dependency](#add-en2do-as-dependency)
  * [Create MongoManager](#create-an-instance-of-the-mongomanager)
  * [Define Entity](#define-an-entity-class)
  * [Create Repository](#create-the-repository-for-the-entity)
  * [Create Scope](#create-the-scope-for-the-entity)
  * [Instantiate objects](#create-object-instances)
* Usages
  * [MongoManager](#)
  * [Repository](#)
  * [Scope](#)

## Features

### Implementation

* Simple conversion of pojo entities

### TODO

* Automatic conversion via PojoCodec of MongoDB (https://www.mongodb.com/developer/languages/java/java-mapping-pojos/)
* Conversion of Enums via Codec
* Better Syntax for Scope usage (via Repository?)
* Spring like https://tuhrig.de/implementing-interfaces-and-abstract-classes-on-the-fly/
  * https://docs.spring.io/spring-data/mongodb/docs/1.2.0.RELEASE/reference/html/mongo.repositories.html

## Get Started

To make it easier to get started with en2do here is a guide to add it to and use it in your project.

### Add en2do as dependency

en2do is hosted and deployed on a private repository. The Repository must be added. 
The examples are designed for a Gradle (Groovy) configuration.

````groovy
repositories {
    maven {
        name 'koboo-reposilite'
        url 'https://reposilite.koboo.eu'
    }
}

dependencies {
    implementation 'eu.koboo:en2do:{version}'
}
````

To get the latest version of en2do look in the repository.

### Create an instance of the ``MongoManager``

In order to connect to the database, a new instance of ``MongoManager`` must be created.

**_Code Example:_**
````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
    }
}
````
If a ``MongoManager`` is created without arguments, a ``MongoConfig`` is automatically created, 
in which the access data of the MongoDB database must be entered.

**_Default ``mongodb.cfg``:_**
````yaml
username: mongodb_user
password: mongodb_password
host: 127.0.0.1
port: 27017
database: mongodb_database
useAuthSource: false
````

The ``MongoConfig`` can also be created and passed as an instance.

**_Code Example:_**
````java
public class Application {
    public static void main(String[] args) {
        MongoConfig mongoConfig = new MongoConfig("mongodb_user", "mongodb_password", "127.0.0.1", "27017", "mongodb_database", false);
        MongoManager manager = new MongoManager(mongoConfig);
    }
}
````

### Define an Entity class

An ``Entity`` can use almost any Java data type. However, there are some special features which are not possible due to MongoDB.

**_Restrictions:_**
* You cannot use a map with numbers as a key.
  * ``Map<Short, ?>``
  * ``Map<Integer, ?>``
  * ``Map<Float, ?>``
  * ``Map<Double, ?>``
  * ``Map<Long, ?>``
  * ``Map<Byte, ?>``

**_Code Example:_**
````java
import eu.koboo.en2do.annotation.*;
import lombok.*;
import java.util.*;

@Getter // generates getter methods (from lombok)
@Setter // generates setter methods (from lombok)
@NoArgsConstructor // generates constructor of entity without parameters (from lombok)
@FieldDefaults(level = AccessLevel.PRIVATE) // Sets the field level to "private" (from lombok)
@ToString // Creates the "toString()" for the entity (from lombok)
@Entity("Customers") // Sets the collection name of the entity (from en2do)
public class Customer {

    @Id // Defines the unique identifier of the entity (from en2do)
    UUID uniqueId;

    int customerId;
    String firstName;
    String lastName;
    String birthday;
    String street;
    int houseNumber;
    Integer postalCode;
    String city;
    Long phoneNumber;
    double balance;
    List<Integer> orderNumbers;
    Map<String, String> orderTexts;
}
````

### Create the Repository for the Entity

In order to access the database and apply operations to an entity, a repository must be defined/created.

**_Code Example:_**
````java
import eu.koboo.en2do.*;
import java.util.*;

public class CustomerRepository extends Repository<Customer, UUID> {

    public CustomerRepository(MongoManager mongoManager) {
        super(mongoManager, Executors.newSingleThreadExecutor());
    }
}
````

### Create the Scope for the Entity

To simplify the use of the framework, a ``Scope`` object was created, which parses the MongoDB fields from the ``Entity``'s methods.

This ``Scope`` object should be created separately for each ``Entity``/``Repository``.

**_Code Example:_**
````java
import eu.koboo.en2do.Scope;
import java.util.UUID;

public class CustomerScope extends Scope<Customer, UUID> {

    public CustomerScope(CustomerRepository repository) {
        super(repository);
    }
}
````

### Create object instances

Now all important classes have been created, and you can start instantiating the objects.

**_Code Example:_**
````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
        
        CustomerRepository repository = new CustomerRepository(manager);
        CustomerScope scope = new CustomerScope(scope);
    }
}
````

## Usages

**_coming soon.._**