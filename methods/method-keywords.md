---
description: All possible method operators.
---

# Method keywords

Here is a listing of all supported methods, and how they are executed in the framework. These methods can be supplemented with any kind of filters. For simplicity, only a `FirstNameEquals` filter is applied.

| Keyword            | Example                                                                      | Method equivalent                                                 |
| ------------------ | ---------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| **findFirstBy**    | `Customer findFirstByFirstName(String firstName)`                            | `collection.find(..).limit(1).first()`                            |
| **findManyBy**     | `List<Customer> findManyByFirstName(String firstName)`                       | `collection.find(..).into(new ArrayList<>())`                     |
| **deleteBy**       | `boolean deleteByFirstName(String firstName)`                                | `collection.deleteMany(..).wasAcknowledged`                       |
| **existsBy**       | `boolean existsByFirstName(String firstName)`                                | `collection.countDocuments(..) > 0`                               |
| **countBy**        | `long countByFirstName(String firstName)`                                    | `collection.countDocuments(..)`                                   |
| **pageBy**         | `List<Customer> pageByFirstName(String firstName, Pagination pagination)`    | `collection.find(..).skip(..).limit(..).into(new ArraysList<>())` |
| **updateFieldsBy** | `boolean updateFieldsByFirstName(String firstName, UpdateBatch updateBatch)` | `collection.updateMany(.., .., ..)`                               |

