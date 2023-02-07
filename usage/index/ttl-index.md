---
description: Using Time-To-Live Index to automatically deleting entities.
---

# TTL Index

Imagine you have an entity, which should be deleted after a specific time. Instead of creating a repeated task in java through a `Timer` or an `ScheduledExecutorService` and using the resources of the application, en2do offers to use MongoDBs time-to-live indexes.

First of all you need to create at least one field of the type `java.util.Date` in your entity.

After that you can decide between two `TTLIndex` options:

1. Delete at timeStamp = `{ttl} {unit} + {timeStamp of field}`
2. Delete at timeStamp = `{timeStamp of field}`

_Example of both time-to-live indexes:_

```java
// Other imports go here...

import java.util.Date;

// Lombok's annotations go here..

// en2do - Expires 10 seconds after timeStamp of "createDate"
@TTLIndex(value = "createTime", ttl = 10, unit = TimeUnit.SECONDS)
// en2do - Expires on timeStamp of "expireDate"
@TTLIndex(value = "expireTime")
public class Customer {

    @Id // en2do
    UUID uniqueId;

    // Other fields go here...

    Date createTime; // Important for 1. TTLIndex annotation
    Date expireTime; // Important for 2. TTLIndex annotation
}
```

[Read more about TTL indexes - MongoDB documentation](https://www.mongodb.com/docs/manual/core/index-ttl/)
