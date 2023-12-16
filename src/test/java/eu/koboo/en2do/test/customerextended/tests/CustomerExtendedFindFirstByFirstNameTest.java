package eu.koboo.en2do.test.customerextended.tests;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customerextended.CustomerExtended;
import eu.koboo.en2do.test.customerextended.CustomerExtendedRepositoryTest;
import eu.koboo.en2do.utility.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerExtendedFindFirstByFirstNameTest extends CustomerExtendedRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        List<CustomerExtended> customerList = repository.findAll();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        Customer customer = Const.createNewCustomer();
        CustomerExtended customerExtended = new CustomerExtended();
        EntityUtils.copyProperties(customer, customerExtended);
        customerExtended.setOrderStatus("Ordered");
        customerExtended.setLockStatus("Not Locked");
        assertNotNull(customerExtended);
        assertTrue(repository.save(customerExtended));
        assertTrue(repository.exists(customerExtended));
    }

    @Test
    @Order(3)
    public void operationTest() {
        CustomerExtended customer = repository.findFirstByFirstName(Const.FIRST_NAME);
        assertNotNull(customer);
        assertEquals("Ordered", customer.getOrderStatus());
        assertEquals("Not Locked", customer.getLockStatus());
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
        assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
        assertEquals(Const.ORDERS.size(), customer.getOrders().size());
    }
}
