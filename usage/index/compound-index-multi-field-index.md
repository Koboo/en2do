---
description: Combining multiple fields to one identifier/index.
---

# Compound Index (Multi-Field-Index)

Since the access does not always necessarily take place on the unique identifier, there is also the possibility to combine several fields at the same time to an index. This annotation is used for this function:

* `@CompoundIndex(value = { @Index("fieldName1"), @Index(value = "fieldName2", ascending = false) }, uniqueIndex = true)`

In the example, an index is created on the `firstName` and `lastName` of the `Customer` entity, which would speed up the method `findFirstByFirstNameAndLastName(String first, String last);`.

It's possible to add multiple `@CompoundIndex` annotations in one entity.

_Example of multiple fields combined to one index:_

```java
// Lombok's annotations go here..

// Creating new multi-field index, a so called CompoundIndex
@CompoundIndex({@Index("firstName"), @Index(value = "lastName", ascending = false)}) // en2do
@CompoundIndex(value = {@Index("uniqueId"), @Index("firstName")}, uniqueIndex = true) // en2do
public class Customer {

    @Id // en2do
    UUID uniqueId;

    String firstName;
    String lastName;
    // Other fields go here...
}
```
