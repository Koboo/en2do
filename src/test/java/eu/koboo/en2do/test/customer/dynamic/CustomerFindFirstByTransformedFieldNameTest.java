package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindFirstByTransformedFieldNameTest extends CustomerRepositoryTest {

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
        customer.setTransformedFieldName("StatusName");
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void operationTest() {
        Customer customer = repository.findFirstByTransformedFieldName("StatusName");
        assertNotNull(customer);
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals("StatusName", customer.getTransformedFieldName());
    }
}
