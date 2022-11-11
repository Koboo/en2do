package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Collection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.DropEntitiesOnStart;
import eu.koboo.en2do.repository.DropIndexesOnStart;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;
import eu.koboo.en2do.sort.object.Sort;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Collection("customer_repository")
@DropIndexesOnStart
@DropEntitiesOnStart
public interface CustomerRepository extends Repository<Customer, UUID> {

    Customer findFirstByFirstName(String firstName);

    long countByFirstName(String firstName);

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

    Customer findFirstByFirstNameRegex(Pattern pattern);

    Customer findFirstByFirstNameExists();

    Customer findFirstByFirstNameContains(String partOfFirstName);

    List<Customer> findManyByBalanceBetweenAndCustomerId(double from, double to, int customerId);

    Customer findFirstByFirstNameAndBalanceNotBetweenAndCustomerId(String firstName, double from, double to, int customerId);

    List<Customer> findManyByCustomerIdOrCustomerId(int customerId1, int customerId2);

    List<Customer> findManyByCustomerIdIn(List<Integer> customerIdList);

    List<Customer> findManyByCustomerIdNotIn(List<Integer> customerIdList);

    @SortBy(field = "customerId")
    @SortBy(field = "balance")
    @Limit(20)
    @Skip(10)
    List<Customer> findManyByCustomerIdExists();

    List<Customer> findManyByCustomerIdNot(int customerId, Sort sort);
}
