---
description: >-
  Sorting entities by using dynamic sort object in repository methods
  parameters.
---

# Sorting by Parameter

Dynamic sorting is provided via the `Sort` method parameter. The `Sort` object and its options can be created in the Fluent pattern.

_Example sorting via parameter:_

```java
public class Application {

    public static void main(String args[]) {
        // Creating instances
        MongoManager manager = new MongoManager();
        CustomerRepository repository = manager.create(CustomerRepository.class);

        // Saving some entities 
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createNewCustomer();
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            repository.save(customer);
        }

        // Sorting them by using sorting parameter
        List<Customer> customerList = repository.findManyByCustomerIdNot(17,
                Sort.create()
                        .order("customerId", false) // by default ascending = true
                        .order("balance")
                        .limit(20)
                        .skip(10)
        );
    }
}
```
