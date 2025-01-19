package eu.koboo.en2do.test.customer.predefined;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerSetUniqueIdTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void testGetUniqueId() {
        Customer customer = Const.createCustomer();
        UUID newId = UUID.randomUUID();
        repository.setUniqueId(customer, newId);
        assertEquals(newId, customer.getUniqueId());
    }
}
