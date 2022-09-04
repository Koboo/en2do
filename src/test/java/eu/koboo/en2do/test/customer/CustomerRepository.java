package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Repository;
import eu.koboo.en2do.sort.Sort;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@eu.koboo.en2do.repository.annotation.Repository("customer_repository")
public interface CustomerRepository extends Repository<Customer, UUID> {

    Customer findByFirstName(String firstName);

    long countByFirstName(String firstName);

    long countByCustomerId(int customerId);

    boolean deleteByFirstName(String firstName);

    boolean existsByLastName(String lastName);

    boolean existsByLastNameContains(String lastNamePart);

    Customer findByFirstNameIgn(String firstname);

    Customer findByBalanceGreaterThan(double balance);

    Customer findByBalanceLessThan(double balance);

    Customer findByBalanceGreaterEq(double balance);

    Customer findByBalanceLessEq(double balance);

    Customer findByFirstNameRegex(String namePart);

    Customer findByFirstNameRegex(Pattern pattern);

    Customer findByFirstNameExists();

    Customer findByFirstNameContains(String partOfFirstName);

    List<Customer> findByBalanceBetweenAndCustomerId(double from, double to, int customerId);

    Customer findByFirstNameAndBalanceNotBetweenAndCustomerId(String firstName, double from, double to, int customerId);

    List<Customer> findByCustomerIdOrCustomerId(int customerId1, int customerId2);

    List<Customer> findByCustomerIdIn(List<Integer> customerIdList);

    List<Customer> findByCustomerIdNotIn(List<Integer> customerIdList);

    @SortBy(field = "customerId")
    @SortBy(field = "balance")
    @Limit(20)
    @Skip(10)
    List<Customer> findByCustomerIdExists();

    List<Customer> findByCustomerIdNot(int customerId, Sort sort);
}