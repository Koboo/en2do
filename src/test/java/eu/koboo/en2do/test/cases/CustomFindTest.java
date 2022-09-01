package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class CustomFindTest {

    static MongoManager manager;
    static CustomerRepository repository;

    @BeforeClass
    public static void before() {
        System.out.println(CustomFindTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(CustomerRepository.class);
        assertNotNull(repository);
    }

    @Test
    public void operationTest() {
        assertTrue(repository.deleteAll());
        Customer original = Const.createNew();
        assertNotNull(original);
        assertTrue(repository.save(original));

        assertTrue(repository.exists(original));

        Customer customer = repository.findById(original.getUniqueId());
        assertNotNull(customer);

        assertEquals(Const.UNIQUE_ID, customer.getUniqueId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
    }
    
    @AfterClass
    public static void after() {
        System.out.println(CustomFindTest.class.getName() + " ending.");
        assertTrue(repository.deleteAll());
        assertTrue(manager.close());
    }
}