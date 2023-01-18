package eu.koboo.en2do.test.customer.predefined;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerDeleteTest extends CustomerRepositoryTest {

    static Customer customer;

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
        customer = Const.createNewCustomer();
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void deleteCustomer() {
        assertTrue(repository.delete(customer));
        assertFalse(repository.existsById(Const.UNIQUE_ID));
        assertEquals(0, repository.countByCustomerId(Const.CUSTOMER_ID));
    }
}
