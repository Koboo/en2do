package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CustomerFindByPaginationTest extends CustomerRepositoryTest {

    static final int CUSTOMER_ID = 3;

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
        for (int i = 0; i < 15; i++) {
            Customer customer = Const.createCustomer();
            assertNotNull(customer);
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void findCustomer() {
        List<Customer> customerList = repository.paginationTest(
            CUSTOMER_ID,
            Pagination.of(5)
                .byField("customerId")
                .setPage(1)
        );
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(5, customerList.size());
        for (Customer customer : customerList) {
            assertNotNull(customer);
            assertNotEquals(CUSTOMER_ID, customer.getCustomerId());
            log.info("customerIds: " + customer.getCustomerId());
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
            assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
            assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        }
    }
}
