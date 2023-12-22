package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindFirstByIdAndOrderTextTest extends CustomerRepositoryTest {

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
        Customer customer = Const.createCustomer();
        assertNotNull(customer);
        customer.setOrder(Const.createOrder());
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void operationTest() {
        Customer customer = repository.findFirstByFirstName(Const.FIRST_NAME);
        assertNotNull(customer);
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertNotNull(customer.getOrder());
        assertNotNull(customer.getOrder().getOrderText());
        assertEquals("My awesome order", customer.getOrder().getOrderText());
    }
}
