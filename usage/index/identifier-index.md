---
description: The unique identifier / "primary key" of the entity.
---

# Identifier Index

By default, the `@Id` field of an entity is indexed. This can be disabled via `@NonIndex` if access should mostly be performed on other fields/queries than the unique identifier.

This should also be done if the `@Id` field isn't unique!

_Example of removing the index of the `@Id` field:_

```java
// Lombok's annotations go here..
public class Customer {

    @Id // en2do - define the identifier of the entity
    @NonIndex // en2do - delete the index of the identifier
    UUID uniqueId;
    
    // Other fields go here...
}

```
