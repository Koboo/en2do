---
description: Appending method names as comment
---

# AppendMethodAsComment

The `@AppendMethodAsComment` annotation appends repository method names to MongoDB Find-Queries to simplify the discovery and capture of slow query operations.

_Example Usage of `@AppendMethodAsComment`:_

```java
@Collection("customer_repository")
@AppendMethodAsComment
public interface CustomerRepository extends Repository<Customer, UUID> {
}
```
