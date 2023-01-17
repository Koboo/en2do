# _// En2Do_

Entity framework for MongoDB in Java 17.

**_En2Do_** is short for **_Entity-To-Document_**.

This framework is heavily inspired by [Spring Data](https://spring.io/projects/spring-data).

You need help or want to share a project, which uses en2do?

[Feel free to join our Discord](https://discord.gg/VGrxZDQu2n)!

## Overview

- [Current Features](#current-features)
- [Add as dependency](#add-as-dependency)
- [Get Started](#get-started)
    - [Create MongoManager](#create-an-instance-of-the-mongomanager)
    - [Define Entity](#define-an-entity-class)
    - [Create Repository](#create-the-repository-for-the-entity)
    - [Instantiate objects](#create-object-instances)
- [Filter keywords](#filter-keywords)
- [Method keywords](#method-keywords)
- [Sorting](#sorting)
    - [Sorting via Annotations](#static-sorting-via-annotations)
    - [Sorting via Parameter](#dynamic-sorting-via-parameter)
- [Indexing](#indexing)
    - [Identifier](#identifier-indexing)
    - [Multi-Field](#multi-field-indexing)
    - [TTLIndex](#ttl-index)
- [Transform](#transform)
- [References](#references)
- [WTFPL License](LICENSE)

## Current Features

- MongoDB POJO-Codec classes ([Learn more](https://www.mongodb.com/developer/languages/java/java-mapping-pojos/))
- Create methods without implementing them ([Learn more](#filter-keywords))
- Create methods with different operations ([Learn more](#method-keywords))
- Repositories by proxy classes to simplify usage and method declaration
- Load credentials from files, resources or hardcoded Strings
- Multiple ways to sort static or dynamic without implementing filters ([Learn more](#sorting))
- Compound Indexes of entity fields ([Learn more](#indexing))

## Add as dependency

**_Gradle (Groovy)_**:

````groovy
repositories {
    maven {
        name 'koboo-reposilite'
        url 'https://reposilite.koboo.eu/releases'
    }
}

dependencies {
    implementation 'eu.koboo:en2do:{version}'
}
````

## Get Started

To make it easier to get started with en2do, here is a guide how to start using it in your project.

### Create an instance of the ``MongoManager``

First of all, you need to connect to the mongo database. Do that by creating a new instance of ``MongoManager``.

**_Code Example:_**

````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
    }
}
````

If a ``MongoManager`` is created without arguments, the credentials are read from the following locations:

1. From Disk: ``{applicationDirectory}/credentials.properties``
2. From Resource: ``{applicationJar}/credentials.properties``

If no credentials are found, an exception is thrown.

Here you can see the default keys of the two required properties.

**_Default ``credentials.properties``:_**

````properties
en2do.connectString=mongodb://<username>:<password>@<host>:<port>/?<options>
en2do.database=<database>
````

You can also create credentials through various other methods.

**_Code-Example:_**

```java
public class Application {
    public static void main(String[] args) {
        Credentials credentials;

        // Keys are "en2do.connectString" and "en2do.database"
        credentials = Credentials.fromResource(); // loads /resources/credentials.properties
        credentials = Credentials.fromResource("/path/to/resources.properties"); // loads from given path

        // Keys are "en2do.connectString" and "en2do.database"
        credentials = Credentials.fromFile(); // loads from {jar-directory}/credentials.properties
        credentials = Credentials.fromFile("/path/to/file.properties"); // loads from given path

        // Keys are "en2do.connectString" and "en2do.database"
        credentials = Credentials.fromSystemProperties();

        // Keys are "EN2DO_CONNECTSTRING" and "EN2DO_DATABASE"
        credentials = Credentials.fromSystemEnvVars();

        credentials = Credentials.of("connectString", "database");
    }
}
```

After you created a ``Credentials`` object, just pass it into the ``MongoManager`` constructor.

**_Code Example:_**

````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager(Credentials.of("connectString", "databaseName"));
    }
}
````

[Learn more about the MongoDB ConnectionString](https://www.mongodb.com/docs/manual/reference/connection-string/)

### Define an Entity class

An ``Entity`` can use almost any Java data type. However, there are can be some special cases which are
not possible. If you found one, let me know and I'll try to implement it.

**The standard PojoCodec of mongodb only allows Strings as keys in maps.**

This has been fixed via a **Custom MapCodec**.
In the [references](#references) you can find all links which helped immensely.

**_Code Example:_**

````java
import eu.koboo.en2do.annotation.*;
import lombok.*;

import java.util.*;

@Getter // from lombok - required (to access fields)
@Setter // from lombok - required (to change fields)
@NoArgsConstructor // from lombok - required (for mongodb, to create instances)
@FieldDefaults(level = AccessLevel.PRIVATE) // from lombok - optional
@ToString // from lombok
public class Customer {

    // from en2do - unique identifier (can be String, int, long, UUID or any object)
    // this will also create an index on this field to speed up queries on the unique identifier
    @Id
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
    List<Order> orders;
}
````

There are also some mapping annotations directly from MongoDB, but en2do only supports one.

- ``@BsonIgnore``, to ignore a field in the entity

**_ATTENTION: You shouldn't use the other annotations, because it could break the mapping of your entity!_**

### Create the Repository for the Entity

If you want to access the database and apply operations to your entity, a repository must be defined.
To ensure type safety, the type of the entity and the type of the identifier must be specified
as type parameters.

**_ATTENTION: First type is the ENTITY, Second type is the KEY of the ENTITY_**

**_Code Example:_**

````java
import eu.koboo.en2do.*;

import java.util.*;

@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

}
````

### Create object instances

Now all important classes have been created, and you bring things together.

**_Code Example:_**

````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
        CustomerRepository repository = manager.create(CustomerRepository.class);
    }
}
````

## Implementation

Here the implementations and keywords are listed and explained.
If a developer should make a mistake, the biggest issues are caught via exceptions
and an error is thrown.

To explain the implemented methods, the [Customer Entity](src/test/java/eu/koboo/en2do/test/customer/Customer.java)
from the [test units](src/test/java/eu/koboo/en2do/test/cases) is used as an example.

Find more examples of method declarations
in [CustomerRepository](src/test/java/eu/koboo/en2do/test/customer/CustomerRepository.java).

## Method keywords

Here is a listing of all supported methods, and how they are executed in the framework.
These methods can be supplemented with any kind of filters. For simplicity, only a ``FirstNameEquals`` filter is
applied.

| Keyword         | Example                                                  | Method equivalent                               |
|-----------------|----------------------------------------------------------|-------------------------------------------------|
| **findFirstBy** | ``Customer findFirstByFirstName(String firstName)``      | ``collection.find(..).limit(1).first()``        |
| **findManyBy**  | ``List<Customer> findManyByFirstName(String firstName)`` | ``collection.find(..).into(new ArrayList<>())`` |
| **deleteBy**    | ``boolean deleteByFirstName(String firstName)``          | ``collection.deleteMany(..).wasAcknowledged``   |
| **existsBy**    | ``boolean existsByFirstName(String firstName)``          | ``collection.countDocuments(..) > 0``           |
| **countBy**     | ``long countByFirstName(String firstName)``              | ``collection.countDocuments(..)``               |

## Filter keywords

| Keyword          | Example                                                                   | Bson equivalent                       |
|------------------|---------------------------------------------------------------------------|---------------------------------------|
| **(No keyword)** | ``findFirstByFirstName(String firstName)``                                | ``Filters.eq``                        |
| **Ign**          | ``findFirstByFirstNameIgn(String firstName)``                             | ``Filters.regex`` (``(?i)^[value]$``) |
| **Contains**     | ``findFirstByFirstNameContains(String part)``                             | ``Filters.regex`` (``.*[value].*``)   |
| **GreaterThan**  | ``findFirstByBalanceGreaterThan(double balance)``                         | ``Filters.gt``                        |
| **LessThan**     | ``findFirstByBalanceLessThan(double balance)``                            | ``Filters.lt``                        |
| **GreaterEq**    | ``findFirstByBalanceGreaterEq(double balance)``                           | ``Filters.gte``                       |
| **LessEq**       | ``findFirstByBalanceLessEq(double balance)``                              | ``Filters.lte``                       |
| **Regex**        | ``findFirstByFirstNameRegex(String regex)``                               | ``Filters.regex``                     |
| **Regex**        | ``findFirstByFirstNameRegex(Pattern pattern)``                            | ``Filters.regex``                     |
| **Exists**       | ``findFirstByFirstNameExists()``                                          | ``Filters.exists``                    |
| **Between**      | ``findFirstByBalanceBetween(double greater, double lower)``               | ``Filters.gt`` + ``Filters.lt``       |
| **BetweenEq**    | ``findFirstByBalanceBetweenEq(double greaterEquals, double lowerEquals)`` | ``Filters.gte`` + ``Filters.lte``     |
| **In**           | ``findFirstByCustomerIdIn(List<Integer> customerIdList)``                 | ``Filters.in``                        |

You can negate any filter with the keyword ``Not``.

| Keyword             | Example                                          | Bson equivalent                                         |
|---------------------|--------------------------------------------------|---------------------------------------------------------|
| **Not(No keyword)** | ``findFirstByFirstNameNot(String firstName)``    | ``Filters.not`` + ``Filters.eq``                        |
| **NotIgn**          | ``findFirstByFirstNameNotIgn(String firstName)`` | ``Filters.not`` + ``Filters.regex`` (``(?i)^[value]$``) |
| ...                 | _This works with every keyword from above_       | ...                                                     |

Filters can also be chained. For this purpose the keyword ``And`` or the keyword ``Or`` can be used per method.

**_ATTENTION: The keywords ``And`` and ``Or`` must not be used in the same method!_**

| Keyword                         | Example                                                                           | Bson equivalent                                                              |
|---------------------------------|-----------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| **Not(No keyword)AndGreaterEq** | ``findFirstByFirstNameNotAndBalanceGreaterEq(String firstName, double balance)``  | ``Filters.not`` + ``Filters.eq`` && ``Filters.gte``                          |
| **IgnAndNotRegex**              | ``findFirstByFirstNameIgnAndLastNameNotRegex(String firstName, String lastName)`` | ``Filters.regex`` (``(?i)^[value]$``) && ``Filters.not`` + ``Filters.regex`` |
| ...                             | _This works with every keyword from above_                                        | ...                                                                          |

_Note: If a method is declared incorrectly, an exception is usually thrown describing the error.
Due to wrong validation checks, this could also occur unintentionally or not at all if the declaration is incorrect._

## Sorting

The framework allows sorting and limiting in 2 ways. These two ways cannot be used at the same time.

## Static sorting via Annotations

When sorting via annotation, the options for sorting must be written statically in annotations,
which means that they can no longer be changed.

| Annotation  | Example                                             | Multiple usage allowed? | Description                                   |
|-------------|-----------------------------------------------------|-------------------------|-----------------------------------------------|
| ``@SortBy`` | ``@SortBy(field = "customerId")``                   | **Yes**                 | Define any entity field to sort by.           |
| ``@SortBy`` | ``@SortBy(field = "customerId", ascending = true)`` | **Yes**                 | Define field and sort direction.              |
| ``@Limit``  | ``@Limit(20)``                                      | **No**                  | Define a maximum amount of returned entities. |
| ``@Skip``   | ``@Skip(10)``                                       | **No**                  | Define an amount of skipped entities.         |

**_Code-Example:_**

````java

@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    @SortBy(field = "customerId")
    @SortBy(field = "balance")
    @Limit(20)
    List<Customer> findManyByCustomerIdExists();
}
````

## Dynamic sorting via Parameter

Dynamic sorting is provided via the ``Sort`` method parameter. The ``Sort`` object and its
options can be created in the Fluent pattern.

**_Code-Example:_**

````java
public class Application {
    public static void main(String args[]) {
        MongoManager manager = new MongoManager();
        CustomerRepository repository = manager.create(CustomerRepository.class);

        List<Customer> customerList = repository.findManyByCustomerIdNot(17,
                Sort.create()
                        .order(ByField.of("customerId", true))
                        .order(ByField.of("balance"))
                        .limit(20)
                        .skip(10)
        );
    }
}
````

## Indexing

MongoDB gives the user the possibility to index desired fields of a document.
([Learn more](https://www.mongodb.com/docs/manual/indexes/))

en2do also enables this feature, but a bit smaller than in native MongoDB.

Indexing allows faster access to entities based on the fields specified in the index.

## Identifier indexing

By default, the ``@Id`` field of an entity is indexed. This can be disabled via ``@NonIndex`` if access should mostly be
performed on other fields/queries than the unique identifier.

This should also be done if the ``@Id`` field isn't unique!

**_Code-Example:_**

````java

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class Customer {

    @Id // en2do
    @NonIndex // en2do
    UUID uniqueId;

    //.. other fields, getter, setter
}

````

## Multi-Field indexing

Since the access does not always necessarily take place on the unique identifier, there is also the possibility to
combine several fields at the same time to an index. This annotation is used for this function:

- ``@CompoundIndex(value = { @Index("fieldName1"), @Index(value = "fieldName2", ascending = false) }, uniqueIndex = true)``

In the example, an index is created on the ``firstName`` and ``lastName`` of the ``Customer`` entity, which would speed
up the method ``findFirstByFirstNameAndLastName(String first, String last);``.

It's possible to add multiple ``@CompoundIndex`` annotations in one entity.

**_Code-Example:_**

````java

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
@CompoundIndex({@Index("firstName"), @Index(value = "lastName", ascending = false)}) // en2do
@CompoundIndex(value = {@Index("uniqueId"), @Index("firstName")}, uniqueIndex = true) // en2do
public class Customer {

    @Id // en2do
    @NonIndex // en2do
    UUID uniqueId;

    String firstName;
    String lastName;
    //.. other fields, getter, setter
}
````

## TTL Index

Imagine you have an entity, which should be deleted after a specific time. Instead of creating a repeated task in java
through a ``Timer`` or an ``ScheduledExecutorService`` and using the resources of the application, en2do offers to use
MongoDBs time-to-live indexes.

First of all you need to create at least one field of the type ``java.util.Date`` in your entity.

After that you can decide between two ``TTLIndex`` options:

1. Delete at timeStamp = ``{ttl} {unit} + {timeStamp of field}``
2. Delete at timeStamp = ``{timeStamp of field}``

**_Code-Example:_**

````java
// Other imports go here...

import java.util.Date;

// Lombok's annotations go here..

// en2do - Expires 10 seconds after timeStamp of "createDate"
@TTLIndex(value = "createTime", ttl = 10, unit = TimeUnit.SECONDS)
// en2do - Expires on timeStamp of "expireDate"
@TTLIndex(value = "expireTime")
public class Customer {

    @Id // en2do
    UUID uniqueId;

    // Other fields go here...

    Date createTime; // Important for 1. TTLIndex annotation
    Date expireTime; // Important for 2. TTLIndex annotation
}
````

[Read more about TTL indexes - MongoDB documentation](https://www.mongodb.com/docs/manual/core/index-ttl/)

## Transform

Sometimes it happens that a method name gets way too long and the code, which uses the method, looks very..
interesting.. (**spaghetti-code**)

To reduce the length of names and make en2do more customizable, I created the ``@Transform`` annotation. Now you can
rename any method to what it really does and just annotate it with ``@Transform("{realMethodNameDeclaration}")`` and
just write the method declaration into the annotation itself.

No worries, en2do still validates all parts of the method name and treats it like a normal delcared method.

**_Code-Example:_**

````java

@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    // Other methods go here...

    @Transform("existsByStreet")
    boolean myTransformedMethod(String street);

    @Transform("findManyByStreet")
    List<Customer> myTransformedMethod2(String street);
}
````

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 Test-Units](https://www.baeldung.com/junit-5-test-byField)
- [MongoDB Documentation](https://www.mongodb.com/docs/manual/introduction/)
- [MongoDB POJO Example](https://www.mongodb.com/developer/languages/java/java-mapping-pojos/)
- [MongoDB POJO Documentation](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/)
- [MongoDB Default POJO Codec](https://github.com/mongodb/mongo-java-driver/tree/master/bson/src/main/org/bson/codecs)
- [MongoDB MapCodec GitHub](https://github.com/benjamonnguyen/mongodb-bson-codec)
- [MongoDB MapCodec StackOverflow](https://stackoverflow.com/questions/67849754/mongodb-mapk-v-codec-maps-must-have-string-keys-fix)
- [Spring MongoDB Repositories](https://docs.spring.io/spring-data/mongodb/docs/1.2.0.RELEASE/reference/html/mongo.repositories.html)
- [Advanced MongoDB Performance Tuning](https://medium.com/idealo-tech-blog/advanced-mongodb-performance-tuning-2ddcd01a27d2)
- [WTFPL License About](http://www.wtfpl.net/)
