---
description: Sorting entities by using static annotations in repository methods.
---

# Sorting by Annotation

When sorting via annotation, the options for sorting must be written statically in annotations, which means that they can no longer be changed.

| Annotation | Example                                           | Multiple usage allowed? | Description                                   |
| ---------- | ------------------------------------------------- | ----------------------- | --------------------------------------------- |
| `@SortBy`  | `@SortBy(field = "customerId")`                   | **Yes**                 | Define any entity field to sort by.           |
| `@SortBy`  | `@SortBy(field = "customerId", ascending = true)` | **Yes**                 | Define field and sort direction.              |
| `@Limit`   | `@Limit(20)`                                      | **No**                  | Define a maximum amount of returned entities. |
| `@Skip`    | `@Skip(10)`                                       | **No**                  | Define an amount of skipped entities.         |

_Example sorting via annotations:_

```java

@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    @SortBy(field = "customerId")
    @SortBy(field = "balance")
    @Limit(20)
    List<Customer> findManyByCustomerIdExists();
}
```
