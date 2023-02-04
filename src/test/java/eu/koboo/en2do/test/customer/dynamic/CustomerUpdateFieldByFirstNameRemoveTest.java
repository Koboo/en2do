package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.repository.methods.fields.FieldUpdate;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerUpdateFieldByFirstNameRemoveTest extends CustomerRepositoryTest {

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
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createNewCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void countCustomer() {
        List<Customer> customerList = repository.findAll();
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(15, customerList.size());
    }

    @Test
    @Order(4)
    public void setFieldValue() {
        assertTrue(repository.updateFieldsByFirstName("Rainer",
                UpdateBatch.of(FieldUpdate.remove("postalCode"))
        ));
    }

    @Test
    @Order(5)
    public void checkFieldValue() {
        List<Customer> customerList = repository.findAll();
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(15, customerList.size());
        for (Customer customer : customerList) {
            assertNull(customer.getPostalCode());
        }
    }
}
