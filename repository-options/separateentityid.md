---
description: Separating entities ids.
---

# SeparateEntityId

The `@SeparateEntityId` annotation is used to generate a separate MongoDB `ObjectID` for each entity of the repository. If `@SeparateEntityId` is not used, the `@Id` field is converted to the `_id` of the MongoDB document and used as a unique index.

_Example Usage of `@SeparateEntityId`:_

```java
@Collection("customer_repository")
@SeparateEntityId
public interface CustomerRepository extends Repository<Customer, UUID> {
}
```

<figure><img src="https://i.imgur.com/xbhxmQa.png" alt=""><figcaption><p><em>A document without <code>@SeparateEntityId</code></em></p></figcaption></figure>

<figure><img src="https://i.imgur.com/Xxakzr0.png" alt=""><figcaption><p><em>A document with <code>@SeparateEntityId</code></em></p></figcaption></figure>

