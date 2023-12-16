package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
@Disabled("Disabled, to speed up unit-testing.")
public class CustomerTTLCreateExpirationTest extends CustomerRepositoryTest {

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
        Customer customer = Const.createNewCustomer();
        assertNotNull(customer);
        assertFalse(repository.exists(customer));
        customer.setCreateTime(new Date());
        assertTrue(repository.save(customer));
        assertTrue(repository.exists(customer));
    }

    @Test
    @Order(3)
    public void validateExpiration() throws Exception {
        assertTrue(repository.existsById(Const.UNIQUE_ID));
        Thread.sleep(TimeUnit.SECONDS.toMillis(120));
        assertFalse(repository.existsById(Const.UNIQUE_ID));
    }
}
