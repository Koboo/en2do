package eu.koboo.en2do.test.customer.dynamic;

import eu.koboo.en2do.repository.methods.sort.Sort;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFindManySortTest extends CustomerRepositoryTest {

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
        for (int i = 15; i > 0; i--) {
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
        Sort sort = Sort.byField("customerId")
            .limit(10)
            .skip(5);
        List<Customer> customerList = repository.findManyByCustomerIdExists(sort);
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(10, customerList.size());
        List<Integer> expectedCustomerIdOrderSet = List.of(
            6, 7, 8, 9, 10, 11, 12, 13, 14, 15
        );
        System.out.println(customerList.stream().map(Customer::getCustomerId).map(String::valueOf).collect(Collectors.toList()));
        for (int i = 0; i < customerList.size(); i++) {
            Customer customer = customerList.get(i);
            assertNotNull(customer);

            int actualCustomerId = customer.getCustomerId();
            assertEquals(expectedCustomerIdOrderSet.get(i), actualCustomerId);

            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
            assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
            assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        }
    }
}