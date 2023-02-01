package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAsyncExistsTest extends CustomerRepositoryTest {

    Customer customer;

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
        customer = Const.createNewCustomer();
        assertNotNull(customer);
        repository.asyncExists(customer).thenAccept(Assertions::assertFalse);
        repository.asyncSave(customer).thenAccept(Assertions::assertTrue);
        repository.asyncExists(customer).thenAccept(Assertions::assertTrue);
    }
}
