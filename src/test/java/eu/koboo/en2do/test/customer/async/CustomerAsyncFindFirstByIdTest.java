package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAsyncFindFirstByIdTest extends CustomerRepositoryTest {

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
        Customer customer = Const.createCustomer();
        assertNotNull(customer);
        repository.asyncExists(customer).thenAccept(Assertions::assertFalse);
        repository.asyncSave(customer).thenAccept(Assertions::assertTrue);
        repository.asyncExists(customer).thenAccept(Assertions::assertTrue);
    }

    @Test
    @Order(3)
    public void findCustomer() {
        repository.asyncExistsById(Const.UNIQUE_ID).thenAccept(Assertions::assertTrue);
        repository.asyncFindFirstById(Const.UNIQUE_ID).thenAccept(customer -> {
            assertNotNull(customer);
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
            assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
            assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        });
    }
}
