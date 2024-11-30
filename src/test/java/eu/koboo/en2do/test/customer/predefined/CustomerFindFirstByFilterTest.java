package eu.koboo.en2do.test.customer.predefined;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindFirstByFilterTest extends CustomerRepositoryTest {

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
        assertFalse(repository.exists(customer));
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void findCustomer() {
        Customer customer = repository.findFirstByFilter(Filters.eq("firstName", Const.FIRST_NAME));
        assertNotNull(customer);
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
    }
}
