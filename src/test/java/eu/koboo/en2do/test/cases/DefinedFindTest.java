package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class DefinedFindTest {

    static MongoManager manager;
    static CustomerRepository repository;

    @BeforeClass
    public static void before() {
        System.out.println(DefinedFindTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(CustomerRepository.class);
        assertNotNull(repository);
    }

    @Test
    public void operationTest() {
        assertTrue(repository.deleteAll());

        Customer one = Const.createNew();
        one.setCustomerId(1);
        assertNotNull(one);
        assertTrue(repository.save(one));
        assertTrue(repository.exists(one));

        Customer two = Const.createNew();
        two.setUniqueId(UUID.randomUUID());
        two.setCustomerId(2);
        assertNotNull(two);
        assertTrue(repository.save(two));
        assertTrue(repository.exists(two));

        List<Customer> customerList = repository.findByCustomerIdEqualsOrCustomerIdEquals(one.getCustomerId(), two.getCustomerId());
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(2, customerList.size());

        for (Customer customer : customerList) {
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
            assertEquals(Const.BIRTHDAY, customer.getBirthday());
        }
    }
    
    @AfterClass
    public static void after() {
        System.out.println(DefinedFindTest.class.getName() + " ending.");
        assertTrue(repository.deleteAll());
        assertTrue(manager.close());
    }
}