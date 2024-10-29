package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindManyListEmptyTest extends CustomerRepositoryTest {

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
        for (int i = 0; i < 10; i++) {
            Customer customer = Const.createCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            if (i % 2 == 0) {
                customer.setOrders(Collections.emptyList());
            }
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void findCustomer() {
        List<Customer> withoutOrders = repository.findManyByOrdersListEmpty();
        assertNotNull(withoutOrders);
        assertEquals(5, withoutOrders.size());

        List<Customer> withOrders = repository.findManyByOrdersNotListEmpty();
        assertNotNull(withOrders);
        assertEquals(5, withOrders.size());
    }
}
