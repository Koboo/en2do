# _// En2Do_

Sync/Async entity framework for mongodb in Java 17

**_En2Do_** is short for **_Entity-To-Document_**.

This framework is heavily inspired by [Spring Data](https://spring.io/projects/spring-data).

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
- [References](#references)
- [WTFPL License](LICENSE)

## Current Features

- MongoDB POJO-Codec classes ([Learn more](https://www.mongodb.com/developer/languages/java/java-mapping-pojos/))
- Create methods without implementing them ([Learn more](#filter-keywords))
- Create methods with different operations ([Learn more](#method-keywords))
- Repositories by proxy classes to simplify usage and method declaration
- Load credentials from files, resources or hardcoded Strings
- Multiple ways to sort static or dynamic without implementing filters ([Learn more](#sorting))

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

In byField to connect to the mongo database, a new instance of ``MongoManager`` must be created.

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

**_Default ``credentials.properties``:_**

````properties
mongodb.connect=mongodb://<username>:<password>@<host>:<port>/?<options>
mongodb.database=<database>
````

The credentials can also hardcoded.

**_Code Example:_**

````java
public class Application {
    public static void main(String[] args) {
        MongoManager manager = new MongoManager("connectString", "databaseName");
    }
}
````

[MongoDB Manual (ConnectionString)](https://www.mongodb.com/docs/manual/reference/connection-string/)

### Define an Entity class

An ``Entity`` can use almost any Java data type. However, there are some special features which are not possible due to
MongoDB.

**_Restrictions:_**

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

There are also some annotations directly from MongoDB, but only one is supported.

- ``@BsonIgnore``, to ignore a sortBy in the entity

**_ATTENTION: You shouldn't use the other annotations, because it could break your entity!_**

### Create the Repository for the Entity

In byField to access the database and apply operations to any entity, a repository must be defined.
To ensure type safety, the type of the entity and the type of the identifier must be specified
as type parameters.

**_Code Example:_**

````java
import eu.koboo.en2do.*;

import java.util.*;

@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

}
````

### Create object instances

Now all important classes have been created, and you can start instantiating the objects.

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
If a developer should make a mistake, the biggest issues are caught via exception throwing and output as an error.

To explain the implemented methods, the [Customer Entity](src/test/java/eu/koboo/en2do/test/customer/Customer.java)
from the [test units](src/test/java/eu/koboo/en2do/test/cases) is used as an example.

Find more examples in [CustomerRepository](src/test/java/eu/koboo/en2do/test/customer/CustomerRepository.java).

## Method keywords

Here is a listing of all supported methods, and how they are executed in the framework.
These methods can be supplemented with any kind of filters. For simplicity, only a ``FirstNameEquals`` filter is
applied.

| Keyword      | Example                                              | Method equivalent                               |
|--------------|------------------------------------------------------|-------------------------------------------------|
| **findBy**   | ``Customer findByFirstName(String firstName)``       | ``collection.find(..).first()``                 |
| **findBy**   | ``List<Customer> findByFirstName(String firstName)`` | ``collection.find(..).into(new ArrayList<>())`` |
| **deleteBy** | ``boolean deleteByFirstName(String firstName)``      | ``collection.deleteMany(..).wasAcknowledged``   |
| **existsBy** | ``boolean existsByFirstName(String firstName)``      | ``collection.find(..).first() != null``         |
| **countBy**  | ``long countByFirstName(String firstName)``          | ``collection.countDocuments(..)``               |

## Filter keywords

| Keyword          | Example                                                               | Bson equivalent                       |
|------------------|-----------------------------------------------------------------------|---------------------------------------|
| **(No keyword)** | ``findByFirstName(String firstName)``                                 | ``Filters.eq``                        |
| **Ign**          | ``findByFirstNameIgn(String firstName)``                              | ``Filters.regex`` (``(?i)^[value]$``) |
| **Contains**     | ``findByFirstNameContains(String part)``                              | ``Filters.regex`` (``.*[value].*``)   |
| **GreaterThan**  | ``findByBalanceGreaterThan(double balance)``                          | ``Filters.gt``                        |
| **LessThan**     | ``findByBalanceLessThan(double balance)``                             | ``Filters.lt``                        |
| **GreaterEq**    | ``findByBalanceGreaterEq(double balance)``                            | ``Filters.gte``                       |
| **LessEq**       | ``findByBalanceLessEq(double balance)``                               | ``Filters.lte``                       |
| **Regex**        | ``findByFirstNameRegex(String regex)``                                | ``Filters.regex``                     |
| **Regex**        | ``findByFirstNameRegex(Pattern pattern)``                             | ``Filters.regex``                     |
| **Exists**       | ``findByFirstNameExists()``                                           | ``Filters.exists``                    |
| **Between**      | ``findByBalanceBetween(double greater, double lower)``                | ``Filters.gt`` + ``Filters.lt``       |
| **BetweenEq**    | ``findByBalanceBetweenEq(double greaterEquals, double lowerEquals)``  | ``Filters.gte`` + ``Filters.lte``     |
| **In**           | ``findByCustomerIdIn(List<Integer> customerIdList)``                  | ``Filters.in``                        |

You can negate any filter with the keyword ``Not``.

| Keyword             | Example                                                              | Bson equivalent                                         |
|---------------------|----------------------------------------------------------------------|---------------------------------------------------------|
| **Not(No keyword)** | ``findByFirstNameNot(String firstName)``                             | ``Filters.not`` + ``Filters.eq``                        |
| **NotIgn**          | ``findByFirstNameNotIgn(String firstName)``                          | ``Filters.not`` + ``Filters.regex`` (``(?i)^[value]$``) |
| ...                 | _This works with every keyword from above_                           | ...                                                     |

Filters can also be chained. For this purpose the keyword ``And`` or the keyword ``Or`` can be used per method.

**_ATTENTION: The keywords ``And`` and ``Or`` must not be used in the same method!_**

| Keyword                         | Example                                                                      | Bson equivalent                                                              |
|---------------------------------|------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| **Not(No keyword)AndGreaterEq** | ``findByFirstNameNotAndBalanceGreaterEq(String firstName, double balance)``  | ``Filters.not`` + ``Filters.eq`` && ``Filters.gte``                          |
| **IgnAndNotRegex**              | ``findByFirstNameIgnAndLastNameNotRegex(String firstName, String lastName)`` | ``Filters.regex`` (``(?i)^[value]$``) && ``Filters.not`` + ``Filters.regex`` |
| ...                             | _This works with every keyword from above_                                   | ...                                                                          |

_Note: If a method is declared incorrectly, an exception is usually thrown describing the error.
Due to wrong validation checks, this could also occur unintentionally or not at all if the declaration is incorrect._

## Sorting

The framework allows sorting in 2 ways. These two ways cannot be used at the same time.

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
public interface CustomerRepository extends Repo<Customer, UUID> {

    @SortBy(field = "customerId")
    @SortBy(field = "balance")
    @Limit(20)
    List<Customer> findByCustomerIdExists();
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

        List<Customer> customerList = repository.findByCustomerIdNot(17,
                Sort.create()
                        .order(ByField.of("customerId", true))
                        .order(ByField.of("balance", true))
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

## Identifier indexing

Basically the ``@Id`` of an entity is indexed. This can be disabled via ``@NonIndex`` if access should mostly be
performed on other fields than the index.

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

- ``@EntityIndex(value = {"fieldName1", "fieldName2"}, ascending = false)``

In the example, an index is created on the ``firstName`` and ``lastName`` of the ``Customer``, which would speed
up the method ``findByFirstNameAndLastName(String first, String last);``.

You can also add multiple ``@EntityIndex`` annotations in one entity.

````java
@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
@EntityIndex(value = {"firstName", "lastName"}, ascending = false) // en2do
public class Customer {

    @Id // en2do
    @NonIndex // en2do
    UUID uniqueId;

    String firstName;
    String lastName;
    //.. other fields, getter, setter
}
````

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 Test-Units](https://www.baeldung.com/junit-5-test-byField)
- [MongoDB Documentation](https://www.mongodb.com/docs/manual/introduction/)
- [MongoDB POJO Example](https://www.mongodb.com/developer/languages/java/java-mapping-pojos/)
- [MongoDB POJO Documentation](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/)
- [MongoDB MapCodec GitHub](https://github.com/benjamonnguyen/mongodb-bson-codec)
- [MongoDB MapCodec StackOverflow](https://stackoverflow.com/questions/67849754/mongodb-mapk-v-codec-maps-must-have-string-keys-fix)
- [Spring MongoDB Repositories](https://docs.spring.io/spring-data/mongodb/docs/1.2.0.RELEASE/reference/html/mongo.repositories.html)
- [WTFPL License About](http://www.wtfpl.net/)
