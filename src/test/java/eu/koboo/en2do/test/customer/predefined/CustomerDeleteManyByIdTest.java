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

public class CustomerDeleteManyByIdTest extends CustomerRepositoryTest {

    static List<UUID> customerIdList;

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
        customerIdList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
            customerIdList.add(customer.getUniqueId());
        }
    }

    @Test
    @Order(3)
    public void deleteAndCountCustomer() {
        assertEquals(15, repository.countAll());
        assertTrue(repository.deleteManyById(customerIdList));
        assertEquals(0, repository.countAll());
    }
}
