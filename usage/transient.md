---
description: Ignoring fields in Documents but not in entities.
---

# Transient

en2do allows you to ignore certain fields in entities. This is made possible by the `@Transient` annotation.

_Example of `@Transient` usage:_

```java
// Lombok's annotations go here..
public class Customer {

    @Id // en2do
    UUID uniqueId;

    // Other fields go here...

    @Transient // en2do - Disable saving to database
    String textNotSavedToDatabase;
}
```
