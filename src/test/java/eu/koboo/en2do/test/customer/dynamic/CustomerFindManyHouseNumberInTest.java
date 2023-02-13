package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindManyHouseNumberInTest extends CustomerRepositoryTest {

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
            Customer customer = Const.createNewCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setHouseNumber(i + 1);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void findCustomer() {
        List<Customer> customerList = repository.findManyByHouseNumberIn(1, 2, 3, 4, 5);
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(5, customerList.size());
        for (Customer customer : customerList) {
            assertNotNull(customer);
            assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
            assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
            assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        }
    }
}
