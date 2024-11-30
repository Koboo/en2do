package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import eu.koboo.en2do.utility.DateUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="GITHUB_ACTIONS", matches = "true", disabledReason = "Disabled locally")
public class CustomerTTLTimestampExpirationTest extends CustomerRepositoryTest {

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
        customer.setExpireTime(DateUtils.createPlus(10, TimeUnit.SECONDS));
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
