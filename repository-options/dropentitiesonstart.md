---
description: Dropping all entities on start.
---

# DropEntitiesOnStart

The `@DropEntitiesOnStart` annotation forces the deletion of all entities when creating the repository.

_**Note: This should only be used for testing or debugging purposes, as ALL entities are permanently deleted!**_

_Example Usage of `@DropEntitiesOnStart`:_

```java
@Collection("customer_repository")
@DropEntitiesOnStart
public interface CustomerRepository extends Repository<Customer, UUID> {
}
```
