package eu.koboo.en2do.test.customer.tests;

import eu.koboo.en2do.sort.object.ByField;
import eu.koboo.en2do.sort.object.Sort;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindByCustomerIdNotSortParameterTest extends CustomerRepositoryTest {

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
        for (int i = 0; i < 100; i++) {
            Customer customer = Const.createNewCustomer();
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void findCustomer() {
        List<Customer> customerList = repository.findByCustomerIdNot(17,
                Sort.create()
                        .order(ByField.of("customerId", true))
                        .limit(20)
                        .skip(10)
        );
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(20, customerList.size());
        for (Customer customer : customerList) {
            assertNotNull(customer);
            assertNotEquals(17, customer.getCustomerId());
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
            assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
            assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        }
    }
}
