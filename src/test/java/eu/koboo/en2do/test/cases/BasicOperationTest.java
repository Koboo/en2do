package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.test.customer.CustomerRepository;
import eu.koboo.en2do.test.customer.CustomerScope;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicOperationTest {

    static MongoManager manager;
    static CustomerRepository repository;
    static CustomerScope scope;

    @BeforeClass
    public static void before() {
        System.out.println(BasicOperationTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = new CustomerRepository(manager);
        assertNotNull(repository);
        scope = new CustomerScope(repository);
        assertNotNull(scope);
    }

    @Test
    public void operationTest() {
        assertTrue(repository.deleteAll());
        Customer original = Const.createNew();
        assertNotNull(original);
        assertTrue(repository.save(original));

        Bson bsonFilter = scope.eq(Customer::getUniqueId, Const.UNIQUE_ID);
        assertTrue(repository.exists(bsonFilter));

        Customer customer = repository.find(bsonFilter);
        assertNotNull(customer);

        assertEquals(Const.UNIQUE_ID, customer.getUniqueId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
    }
    
    @AfterClass
    public static void after() {
        System.out.println(BasicOperationTest.class.getName() + " ending.");
        assertTrue(repository.deleteAll());
        assertTrue(manager.close());
    }
}