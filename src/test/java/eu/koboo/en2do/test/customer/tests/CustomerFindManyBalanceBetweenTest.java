package eu.koboo.en2do.test.customer.tests;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindManyBalanceBetweenTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        List<Customer> customerList = repository.findMany();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        Customer customer = Const.createNewCustomer();
        assertNotNull(customer);
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void operationTest() {
        List<Customer> customerList = repository.findManyByBalanceBetweenAndCustomerId(100, 600, 1);
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(1, customerList.size());
        Customer customer = customerList.get(0);
        assertNotNull(customer);
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
        assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
        assertEquals(Const.ORDERS.size(), customer.getOrders().size());
    }
}
