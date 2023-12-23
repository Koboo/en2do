package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAsyncDeleteManyByIdTest extends CustomerRepositoryTest {

    static List<UUID> customerIdList;

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
        customerIdList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            repository.asyncSave(customer).thenAccept(Assertions::assertTrue);
            repository.asyncExists(customer).thenAccept(Assertions::assertTrue);
            customerIdList.add(customer.getUniqueId());
        }
    }

    @Test
    @Order(3)
    public void deleteAndCountCustomer() {
        repository.asyncCountAll().thenAccept(count -> assertEquals(15, count));
        repository.asyncDeleteManyById(customerIdList).thenAccept(Assertions::assertTrue);
        repository.asyncCountAll().thenAccept(count -> assertEquals(0, count));
    }
}
