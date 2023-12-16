package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.repository.methods.sort.Sort;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAsyncSortAllTest extends CustomerRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        repository.asyncFindAll().thenAccept(customerList -> {
            assertNotNull(customerList);
            assertTrue(customerList.isEmpty());
        });
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createNewCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            repository.asyncSave(customer).thenAccept(Assertions::assertTrue);
            repository.asyncExists(customer).thenAccept(Assertions::assertTrue);
        }
    }

    @Test
    @Order(3)
    public void findCustomer() {
        repository.asyncSortAll(Sort.of().order("customerId", true).limit(10).skip(5)).thenAccept(customerList -> {
            assertNotNull(customerList);
            assertFalse(customerList.isEmpty());
            assertEquals(5, customerList.size());
            for (Customer customer : customerList) {
                assertNotNull(customer);
                assertEquals(Const.FIRST_NAME, customer.getFirstName());
                assertEquals(Const.LAST_NAME, customer.getLastName());
                assertEquals(Const.BIRTHDAY, customer.getBirthday());
                assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
                assertEquals(Const.ORDERS.size(), customer.getOrders().size());
            }
        });
    }
}
