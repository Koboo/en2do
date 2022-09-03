# _// En2Do_

Sync/Async entity framework for mongodb in Java 17

**_En2Do_** is short for **_Entity-To-Document_**.

## Overview
* Features
  * [What can it do?](#what-can-it-do-current-implementations)
  * [What should it do?](#what-should-it-do-future-implementations)
* Get Started
  * [Add as dependency](#add-en2do-as-dependency)
  * [Create MongoManager](#create-an-instance-of-the-mongomanager)
  * [Define Entity](#define-an-entity-class)
  * [Create Repository](#create-the-repository-for-the-entity)
  * [Instantiate objects](#create-object-instances)
* Implementation
  * [Filter Keywords](#implemented-filter-keywords)
  * [Methods](#implemented-methods)
  * [Scope](#)

## Features

### What can it do (Current implementations)

* MongoDB Conversion of POJO classes (POJOs in POJOs in POJOs)
* Create filters without implementing them
* Object creation by proxy classes to simplify usage and method declaration
* Implemented filters, which can be chained, negated and executed together to get the expected results
* Load credentials from disk-files, resource-files or insert as hardcoded strings 

### What should it do (Future implementations)

* Repository task flushing
* Operator ``countBy``
* Operator ``existsBy``

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
If a ``MongoManager`` is created without arguments, the credentials are read from the following locations:
1. From Disk: ``{applicationDirectory}/credentials.properties``
2. From Resource: ``{applicationJar}/credentials.properties``

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

[To get help about the ConnectionString, see MongoDB Manual.](https://www.mongodb.com/docs/manual/reference/connection-string/)

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

@Getter // from lombok - required (to access fields)
@Setter // from lombok - required (to change fields)
@NoArgsConstructor // from lombok - required (for mongodb, to create instances)
@FieldDefaults(level = AccessLevel.PRIVATE) // from lombok - optional
@ToString // from lombok
public class Customer {

  @Id // from en2do - unique identifier (can be String, int, long, UUID or any object)
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

### Create the Repository for the Entity

In order to access the database and apply operations to any entity, a repository must be defined.
To ensure type safety, the type of the entity and the type of the identifier must be specified 
as type parameters.

**_Code Example:_**
````java
import eu.koboo.en2do.*;
import java.util.*;

@Repository("customer_repository")
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

Here the implementations of the framework are listed and roughly explained.
If a developer should make a mistake, the biggest issues are caught via exception throwing and output as an error.

To explain the implemented methods, the [Customer Entity](src/test/java/eu/koboo/en2do/test/customer/Customer.java)
from the test units is used as an example.

### Implemented methods

Here is a listing of all supported methods, and how they are executed in the framework. 
These methods can be supplemented with any kind of filters. For simplicity, only a ``FirstNameEquals`` filter is applied.

| Keyword      | Example                                              | Method equivalent                               |
|--------------|------------------------------------------------------|-------------------------------------------------|
| ``findBy``   | ``Customer findByFirstName(String firstName)``       | ``collection.find(..).first()``                 |
| ``findBy``   | ``List<Customer> findByFirstName(String firstName)`` | ``collection.find(..).into(new ArrayList<>())`` |
| ``deleteBy`` | ``boolean deleteByFirstName(String firstName)``      | ``collection.deleteMany(..).wasAcknowledged``   |
| ``existsBy`` | ``boolean existsByFirstName(String firstName)``      | ``collection.find(..).first() != null``         |
| ``countBy``  | ``long countByFirstName(String firstName)``          | ``collection.countDocuments(..)``               |

### Implemented filter keywords

#### Filter Keyword Cheatsheet

| Keyword      | Example                                                               | Bson equivalent                       |
|--------------|-----------------------------------------------------------------------|---------------------------------------|
| (No keyword) | ``findByFirstName(String firstName)``                                 | ``Filters.eq``                        |
| Ign          | ``findByFirstNameIgn(String firstName)``                              | ``Filters.regex`` (``(?i)^[value]$``) |
| Contains     | ``findByFirstNameContains(String part)``                              | ``Filters.regex`` (``.*[value].*``)   |
| GreaterThan  | ``findByBalanceGreaterThan(double balance)``                          | ``Filters.gt``                        |
| LessThan     | ``findByBalanceLessThan(double balance)``                             | ``Filters.lt``                        |
| GreaterEq    | ``findByBalanceGreaterEq(double balance)``                            | ``Filters.gte``                       |
| LessEq       | ``findByBalanceLessEq(double balance)``                               | ``Filters.lte``                       |
| Regex        | ``findByFirstNameRegex(String regex)``                                | ``Filters.regex``                     |
| Regex        | ``findByFirstNameRegex(Pattern pattern)``                             | ``Filters.regex``                     |
| Exists       | ``findByFirstNameExists()``                                           | ``Filters.exists``                    |
| Between      | ``findByBalanceBetween(double greater, double lower)``                | ``Filters.gt`` + ``Filters.lt``       |
| BetweenEq    | ``findByBalanceBetweenEq(double greaterEquals, double lowerEquals)``  | ``Filters.gte`` + ``Filters.lte``     |
| In           | ``findByCustomerIdIn(List<Integer> customerIdList)``                  | ``Filters.in``                        |

You can negate any filter with the keyword ``Not``.

| Keyword         | Example                                                              | Bson equivalent                                         |
|-----------------|----------------------------------------------------------------------|---------------------------------------------------------|
| Not(No keyword) | ``findByFirstNameNot(String firstName)``                             | ``Filters.not`` + ``Filters.eq``                        |
| NotIgn          | ``findByFirstNameNotIgn(String firstName)``                          | ``Filters.not`` + ``Filters.regex`` (``(?i)^[value]$``) |
| ...             | _This works with every keyword from above_                           | ...                                                     |

Filters can also be chained. For this purpose the keyword ``And`` or the keyword ``Or`` can be used per method.

**_ATTENTION: The keywords ``And`` and ``Or`` must not be used in the same method!_**

| Keyword                     | Example                                                                      | Bson equivalent                                                              |
|-----------------------------|------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| Not(No keyword)AndGreaterEq | ``findByFirstNameNotAndBalanceGreaterEq(String firstName, double balance)``  | ``Filters.not`` + ``Filters.eq`` && ``Filters.gte``                          |
| IgnAndNotRegex              | ``findByFirstNameIgnAndLastNameNotRegex(String firstName, String lastName)`` | ``Filters.regex`` (``(?i)^[value]$``) && ``Filters.not`` + ``Filters.regex`` |
| ...                         | _This works with every keyword from above_                                   | ...                                                                          |

[Find more examples in CustomerRepository](src/test/java/eu/koboo/en2do/test/customer/CustomerRepository.java)

_Note: If a method is declared incorrectly, an exception is usually thrown describing the error. 
Due to wrong validation checks, this could also occur unintentionally or not at all if the declaration is incorrect._

## References

* [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
* [JUnit 5 Test-Units](https://www.baeldung.com/junit-5-test-order)
* [MongoDB POJO](https://www.mongodb.com/developer/languages/java/java-mapping-pojos/)
* [Spring MongoDB Repositories](https://docs.spring.io/spring-data/mongodb/docs/1.2.0.RELEASE/reference/html/mongo.repositories.html)