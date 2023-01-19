package eu.koboo.en2do.test.customer.predefined;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerDeleteAllTest extends CustomerRepositoryTest {

    static List<Customer> customerList;

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
        customerList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createNewCustomer();
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
            customerList.add(customer);
        }
    }

    @Test
    @Order(3)
    public void deleteAndCountCustomer() {
        assertEquals(15, repository.countAll());
        assertTrue(repository.deleteAll(customerList));
        assertEquals(0, repository.countAll());
    }
}
