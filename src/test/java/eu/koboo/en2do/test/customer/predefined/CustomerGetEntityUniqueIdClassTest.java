package eu.koboo.en2do.test.customer.predefined;

import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerGetEntityUniqueIdClassTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void testGetEntityUniqueIdClass() {
        assertEquals(UUID.class, repository.getEntityUniqueIdClass());
    }
}
