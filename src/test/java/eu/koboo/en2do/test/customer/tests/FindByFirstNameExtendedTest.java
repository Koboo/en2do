package eu.koboo.en2do.test.customer.tests;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerExtended;
import eu.koboo.en2do.test.customer.CustomerExtendedRepository;
import eu.koboo.en2do.utility.EntityUtils;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FindByFirstNameExtendedTest {

    static MongoManager manager;
    static CustomerExtendedRepository repository;

    @BeforeAll
    public static void setup() {
        System.out.println(FindByFirstNameExtendedTest.class.getName() + " START");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(CustomerExtendedRepository.class);
        assertNotNull(repository);
    }

    @Test
    @Order(1)
    public void cleanUpRepository() {
        assertTrue(repository.drop());
        List<CustomerExtended> customerList = repository.findAll();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        Customer customer = Const.createNew();
        CustomerExtended customerExtended = new CustomerExtended();
        EntityUtils.copyProperties(customer, customerExtended);
        assertNotNull(customerExtended);
        assertTrue(repository.save(customerExtended));
        assertTrue(repository.exists(customerExtended));
    }

    @Test
    @Order(3)
    public void operationTest() {
        CustomerExtended customer = repository.findByFirstName(Const.FIRST_NAME);
        assertNotNull(customer);
        assertEquals(Const.CUSTOMER_ID, customer.getCustomerId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
        assertEquals(Const.PHONE_NUMBER, customer.getPhoneNumber());
        assertEquals(Const.ORDERS.size(), customer.getOrders().size());
        assertNull(customer.getOrderStatus());
        assertNull(customer.getLockStatus());
    }

    @AfterAll
    public static void finish() {
        System.out.println(FindByFirstNameExtendedTest.class.getName() + " END");
        assertTrue(repository.drop());
        assertTrue(manager.close());
    }
}
