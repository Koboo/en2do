---
description: Dropping all indexes on start
---

# DropIndexesOnStart

The `@DropIndexesOnStart` annotation forces the deletion of all created indexes when creating the repository.

_**Note: This should only be used for testing or debugging purposes, as ALL indexes are permanently deleted!**_

_Example Usage of `@DropIndexesOnStart`:_

```java
@Collection("customer_repository")
@DropIndexesOnStart
public interface CustomerRepository extends Repository<Customer, UUID> {
}
```
