---
description: Renaming fields in Documents.
---

# TransformField

You can transform not only method names with en2do, but also field names. More precisely, the names of the fields in the MongoDB document. This is made possible by the `@TransformField` method.

_Example of `@TransformField` usage:_

```java
// Lombok's annotations go here.. 
public class Customer {

    @Id // en2do
    UUID uniqueId;

    // Other fields go here...

    @TransformField("otherField") // en2do - Change field name to "otherField"
    String reallyLongFieldName;
}
```
