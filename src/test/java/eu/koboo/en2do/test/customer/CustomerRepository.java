package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Limit;
import eu.koboo.en2do.repository.methods.sort.Skip;
import eu.koboo.en2do.repository.methods.sort.Sort;
import eu.koboo.en2do.repository.methods.sort.SortBy;
import eu.koboo.en2do.repository.methods.transform.NestedField;
import eu.koboo.en2do.repository.methods.transform.Transform;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@Collection("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID>, AsyncRepository<Customer, UUID> {

    Customer findFirstByFirstNameAndUniqueId(String firstName, UUID uniqueId);

    Customer findFirstByFirstName(String firstName);

    long countByFirstName(String firstName);

    @Transform("countByCustomerIdExistsAndCustomerId")
    @Async
    CompletableFuture<Long> asyncCountCustomerId(int customerId);

    long countByCustomerId(int customerId);

    boolean deleteByFirstName(String firstName);

    boolean existsByLastName(String lastName);

    boolean existsByLastNameContains(String lastNamePart);

    Customer findFirstByFirstNameIgn(String firstname);

    Customer findFirstByBalanceGreaterThan(double balance);

    Customer findFirstByBalanceLessThan(double balance);

    Customer findFirstByBalanceGreaterEq(double balance);

    Customer findFirstByBalanceLessEq(double balance);

    Customer findFirstByFirstNameRegex(String namePart);

    Customer findFirstByFirstNameExists();

    List<Customer> findManyByFirstNameExists();

    List<Customer> findTop20ByFirstNameExists();

    Customer findFirstByFirstNameContains(String partOfFirstName);

    List<Customer> findManyByBalanceBetweenAndCustomerId(double from, double to, int customerId);

    Customer findFirstByFirstNameAndBalanceNotBetweenAndCustomerId(String firstName, double from, double to, int customerId);

    List<Customer> findManyByCustomerIdOrCustomerId(int customerId1, int customerId2);

    List<Customer> findManyByCustomerIdIn(List<Integer> customerIdList);

    List<Customer> findManyByCustomerIdNotIn(List<Integer> customerIdList);

    List<Customer> findManyByHouseNumberIn(Integer... houseNumberList);

    Customer findFirstByDescriptionHasKey(UUID version);

    Customer findFirstByIdListHas(UUID id);

    @SortBy(field = "customerId")
    @SortBy(field = "balance", ascending = true)
    @Limit(10)
    @Skip(5)
    List<Customer> findManyByCustomerIdExists();

    List<Customer> findManyByCustomerIdNot(int customerId, Sort sort);

    @Transform("existsByStreet")
    boolean myTransformedMethod(String street);

    @Transform("findManyByStreet")
    List<Customer> myTransformedMethod2(String street);

    List<Customer> pageByCustomerIdNot(int customerId, Pagination sorter);

    boolean updateFieldsByFirstName(String firstName, UpdateBatch updateBatch);

    Customer findFirstByTransformedFieldName(String status);

    @NestedField(key = "KeyToIdentify", query = "order.orderText")
    Customer findFirstByUniqueIdAndKeyToIdentify(UUID uniqueId, String orderText);

    Customer findFirstByFirstNameIsNull();

    Customer findFirstByFirstNameNonNull();

    Customer findFirstByLockedIsTrue();

    Customer findFirstByLockedIsFalse();

    List<Customer> findManyByOrdersListEmpty();

    List<Customer> findManyByOrdersNotListEmpty();
}
