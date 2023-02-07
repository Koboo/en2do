---
description: Updating fields of documents.
---

# UpdateBatch

To make it easier to work with individual fields, the native MongoDB driver has the `updateMany` method, which can be used to filter and then update documents.

This is provided in en2do via the `updateFieldsBy` method operator or via the predefined `updateAllFields(UpdateBatch updateBatch)` method.

_Example of using `UpdateBatch`:_

```java
// Remove the field "postalCode"
boolean success = repository.updateAllFields(UpdateBatch.of(FieldUpdate.remove("postalCode")));

// Rename the field "balance" to "balanceRenamed"
boolean success = repository.updateAllFields(UpdateBatch.of(FieldUpdate.rename("balance", "balanceRenamed"));

// Hard-Set the value of the field "postalCode" to 987654321
boolean success = repository.updateAllFields(UpdateBatch.of(FieldUpdate.set("postalCode", 987654321)));
```

To optimize performance in this respect, several `FieldUpdate` can be combined in one call.

_Example of multiple `FieldUpdate` in one call:_

```java
boolean success = repository.updateAllFields(UpdateBatch.of(
                FieldUpdate.remove("postalCode"),
                FieldUpdate.rename("balance", "balanceRenamed"),
                FieldUpdate.set("postalCode", 987654321)
        ));

```
