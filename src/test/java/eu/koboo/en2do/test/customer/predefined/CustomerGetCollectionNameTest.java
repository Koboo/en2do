package eu.koboo.en2do.test.customer.predefined;

import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerGetCollectionNameTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void testGetCollectionName() {
        assertEquals("customer_repository", repository.getCollectionName());
    }
}
