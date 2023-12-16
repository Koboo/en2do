package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAsyncDeleteTest extends CustomerRepositoryTest {

    static Customer customer;

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
        repository.asyncSave(customer).thenAccept(Assertions::assertTrue);
        repository.asyncExists(customer).thenAccept(Assertions::assertTrue);
    }

    @Test
    @Order(3)
    public void deleteCustomer() {
        repository.asyncDelete(customer).thenAccept(Assertions::assertTrue);
        repository.asyncExistsById(Const.UNIQUE_ID).thenAccept(Assertions::assertFalse);
        repository.asyncCountCustomerId(Const.CUSTOMER_ID).thenAccept(count -> assertEquals(0, count));
    }
}
