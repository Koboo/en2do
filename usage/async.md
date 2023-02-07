---
description: Making Repository methods asynchronous.
---

# Async

You can have individual methods in a `repository` executed asynchronously, regardless of whether you have passed your own ExecutorService in the MongoManager or not.

This works via the `@Async` annotation, via the desired method in the `Repository`. However, the method only returns a `CompletableFuture` with its value instead of the value directly.

The only thing that changes in the method declaration is the return value, which wraps the previous one in a CompletableFuture.

_Example of using the `@Async`:_

```java
@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID>, AsyncRepository<Customer, UUID> {

    // Customer findFirstByFirstName(String firstName);
    @Async
    CompletableFuture<Customer> findFirstByFirstName(String firstName);

    // List<Customer> findManyByCustomerIdIn(List<Integer> customerIdList);
    @Async
    CompletableFuture<List<Customer>> findManyByCustomerIdIn(List<Integer> customerIdList);

    List<Customer> findManyByCustomerIdNotIn(List<Integer> customerIdList);

    // List<Customer> findManyByCustomerIdExists();
    @SortBy(field = "customerId")
    @SortBy(field = "balance", ascending = true)
    @Limit(10)
    @Skip(5)
    @Async
    CompletableFuture<List<Customer>> findManyByCustomerIdExists();

    List<Customer> findManyByCustomerIdNot(int customerId, Sort sort);

    // boolean myTransformedMethod(String street);
    @Transform("existsByStreet")
    @Async
    CompletableFuture<Boolean> myTransformedMethod(String street);

    // boolean updateFieldsByFirstName(String firstName, UpdateBatch updateBatch);
    @Async
    CompletableFuture<Boolean> updateFieldsByFirstName(String firstName, UpdateBatch updateBatch);
}

```
