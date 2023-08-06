package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindFirstByDescriptionHasKeyTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        List<Customer> customerList = repository.findAll();
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
        Customer customer = repository.findFirstByDescriptionHasKey(1);
        assertNotNull(customer);
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
        assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
        assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        assertEquals(Const.DESCRIPTION.size(), customer.getDescription().size());
        assertEquals(3, customer.getDescription().size());
        assertEquals(Const.DESCRIPTION.get(1), customer.getDescription().get(1));
    }
}
