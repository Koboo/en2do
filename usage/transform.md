---
description: Renaming Repository methods.
---

# Transform

Sometimes it happens that a method name gets way too long and the code, which uses the method, looks very.. interesting.. (**spaghetti-code**)

To reduce the length of names and make en2do more customizable, I created the `@Transform` annotation.&#x20;

Now you can rename any method to what it really does and just annotate it with&#x20;

* `@Transform("{realMethodNameDeclaration}")`&#x20;

and just write the method declaration into the annotation itself.\


_**ATTENTION: No worries, en2do still validates all parts of the method name and treats it like a normal declared method.**_\
_****_

_Example of transforming method names:_

```java
@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    // Other methods go here...

    @Transform("existsByStreet")
    boolean myTransformedMethod(String street);

    @Transform("findManyByStreet")
    List<Customer> myTransformedMethod2(String street);
}
```
