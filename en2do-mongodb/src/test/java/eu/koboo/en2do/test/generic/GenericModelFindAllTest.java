package eu.koboo.en2do.test.generic;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GenericModelFindAllTest extends GenericModelRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        List<GenericModelImpl> customerList = repository.findAll();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        for (int i = 0; i < 15; i++) {
            GenericModelImpl customer = new GenericModelImpl();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setProperty("Test");
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void countCustomer() {
        List<GenericModelImpl> customerList = repository.findAll();
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(15, customerList.size());
        assertEquals("Test", customerList.get(0).getProperty());
    }
}
