# Create an Entity

An `Entity` can use almost any Java data type. However, there are can be some special cases which are not possible.&#x20;

E.g. The standard PojoCodec of mongodb only allows `String` as keys in `Map`. This was using a **Custom MapCodec**. In the references on github you can find all links which helped immensely.

If you found one any other incompatible data type, let me know and I'll implement it.

### Requirements for entities

All entities must fulfil the following properties

1. Only one constructor, which is `public` and has no arguments, is existing
2. There is exactly 1 field with the `@Id` annotation to define the unique identifier.
3. All fields are not `final` !

Except for the properties above, the entity can be adapted dynamically.

_Example entity `Customer`:_

```java
import eu.koboo.en2do.annotation.*;
import lombok.*;

import java.util.*;

@Getter // from lombok - required (to access fields)
@Setter // from lombok - required (to change fields)
@NoArgsConstructor // from lombok - required (for mongodb, to create instances)
@FieldDefaults(level = AccessLevel.PRIVATE) // from lombok - optional
@ToString // from lombok
public class Customer {

    // from en2do - unique identifier (can be String, int, long, UUID or any object)
    // this will also create an index on this field to speed up queries on the unique identifier
    @Id
    UUID uniqueId;

    int customerId;
    String firstName;
    String lastName;
    String birthday;
    String street;
    int houseNumber;
    Integer postalCode;
    String city;
    Long phoneNumber;
    double balance;
    List<Order> orders;
}
```

_**ATTENTION: There are also some Annotations from the MongoDB driver, but en2do doesn't support them. Please only use the Annotations from en2do directly!**_
