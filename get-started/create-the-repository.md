# Create the Repository

If you want to access the database and apply operations to your entity, a repository must be defined.&#x20;

Keep in mind that the repository isn't a `class`. It's just an `interface`!

To ensure type safety, the type of the entity and the identifier must be specified as type parameters of the `Repository<E, K>` interface.

_**ATTENTION:**_&#x20;

1. _**First type is the ENTITY (**_`<E>`_**)**_
2. _**Second type is the KEY of the ENTITY (**_`<K>`_**)**_

_Example repository `CustomerRepository`:_

```java
import eu.koboo.en2do.*;
import java.util.*;
 
//          Name of the collection in database
@Collection("customer_repository")//                   Entity    Key / Identifier
public interface CustomerRepository extends Repository<Customer, UUID> {

}
```
