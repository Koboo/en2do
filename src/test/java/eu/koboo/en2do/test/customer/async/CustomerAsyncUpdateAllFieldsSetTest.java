package eu.koboo.en2do.test.customer.async;

import eu.koboo.en2do.repository.methods.fields.FieldUpdate;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerAsyncUpdateAllFieldsSetTest extends CustomerRepositoryTest {

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
    public void countCustomer() {
        repository.asyncCountAll().thenAccept(count -> assertEquals(15, count));
    }

    @Test
    @Order(4)
    public void setFieldValue() {
        repository.asyncUpdateAllFields(UpdateBatch.of(FieldUpdate.set("postalCode", 987654321)))
            .thenAccept(Assertions::assertTrue);
    }

    @Test
    @Order(5)
    public void checkFieldValue() {
        repository.asyncFindAll().thenAccept(customerList -> {
            assertNotNull(customerList);
            assertFalse(customerList.isEmpty());
            assertEquals(15, customerList.size());
            for (Customer customer : customerList) {
                assertEquals(0, customer.getBalance());
                assertEquals(Const.BALANCE, customer.getBalanceRenamed());
            }
        });
    }
}
