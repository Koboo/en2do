package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.AsyncCustomerRepositoryTest;
import eu.koboo.en2do.test.customer.Customer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncCustomerFindFirstByIdTest extends AsyncCustomerRepositoryTest {

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
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void findCustomerById() {
        repository.findFirstByUniqueId(Const.UNIQUE_ID)
            .whenComplete((customer, throwable) -> {
                assertNotNull(customer);
                UUID uniqueId = customer.getUniqueId();
                assertNotNull(uniqueId);
                assertEquals(Const.UNIQUE_ID, uniqueId);
                System.out.println("Asserted!");
            });
    }
}
