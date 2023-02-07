---
description: Paging entities with or without filters.
---

# Pagination

To simplify pagination there is the `pageBy` method operator. This enables the pagination of individual repositories. In addition to the dynamic method operator, there is also the `pageAll` method, which works without a filter.

A `pageBy` method must always have a `pagination` object as the last parameter, where the properties of the pagination are set. The `Pagination` object cannot be used in any other method operator.

_Example of the dynamic `pageBy` method_

```java
@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    List<Customer> pageByCustomerIdNot(int customerId, Pagination pagination);
}
```

_Example usage of pagination methods_

```java
public class Application {

    public static void main(Stribng[] args) {
        // Creating instances
        MongoManager mongoManager = new MongoManager();
        CustomerRepository repository = mongoManager.create(CustomerRepository.class); 
        
        // Saving some entities 
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createNewCustomer();
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            repository.save(customer);
        }
        
        // Creating the pagination properties
        int entitiesPerPage = 5;
        int currentPage = 2;

        // Creating the pagination object        
        Pagination pagination = Pagination.of(entitiesPerPage).page(currentPage);
        
        // Pagination by pageAll without any filter
        List<Customer> allCustomersPage = repository.pageAll(pagination);
        
        // Pagination by dynamic method with filter on customer id
        List<Customer> customerWithoutIdZeroPage = repository.pageByCustomerIdNot(0, pagination);
     }
}
```
