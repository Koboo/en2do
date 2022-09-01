package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.annotation.Repository;
import eu.koboo.en2do.Repo;

import java.util.List;
import java.util.UUID;

@Repository("customer_repository")
public interface CustomerRepository extends Repo<Customer, UUID> {

    /*

    // Every Operator with Not also works
    * Eq
    * EqIgn
    * GreaterThan
    * GreaterEq
    * LessThan
    * LessEq
    * Regex
    * Has

    * In findByCustomerIdIn(List<Integer> customerId);

     */

    Customer findByCustomerIdEquals(int customerId);

    Customer findByFirstNameEquals(String test);

    Customer findByFirstNameHas();

    List<Customer> findByCustomerIdNotEquals(int customerId);

    List<Customer> findByCustomerIdEqualsOrCustomerIdEquals(int customerId1, int customerId2);
}