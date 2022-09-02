package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.SortOptions;
import eu.koboo.en2do.annotation.Repository;
import eu.koboo.en2do.Repo;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Repository("customer_repository")
public interface CustomerRepository extends Repo<Customer, UUID> {

    Customer findByFirstName(String firstName);

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

    List<Customer> findByCustomerIdNot(int customerId, SortOptions<Customer> sortOptions);

    List<Customer> findByCustomerIdOrCustomerId(int customerId1, int customerId2);

    //List<Customer> findByCustomerIdIn(List<Integer> customerIdList);

    List<Customer> findByCustomerIdExists(SortOptions<Customer> sortOptions);
}