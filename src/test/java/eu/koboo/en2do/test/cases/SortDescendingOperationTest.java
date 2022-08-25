package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import eu.koboo.en2do.test.customer.CustomerScope;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class SortDescendingOperationTest {

    static int documentsCount = 20;
    static int maxDocuments = 10;
    static MongoManager manager;
    static CustomerRepository repository;
    static CustomerScope scope;

    @BeforeClass
    public static void before() {
        System.out.println(SortDescendingOperationTest.class.getName() + " starting.");
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
        for(int i = 0; i < documentsCount; i++) {
            System.out.println("Create Customer #" + i);
            Customer customer = Const.createNew();
            assertNotNull(customer);

            UUID newUniqueId = UUID.randomUUID();
            customer.setUniqueId(newUniqueId);
            assertEquals(customer.getUniqueId(), newUniqueId);

            customer.setCustomerId(i);
            assertEquals(customer.getCustomerId(), i);

            assertTrue(repository.save(customer));
        }

        Bson hasIdFilter = scope.has(Customer::getCustomerId);
        assertNotNull(hasIdFilter);

        List<Customer> customerList = repository.findSortLimit(hasIdFilter, scope.sort(Customer::getCustomerId, false), maxDocuments);
        assertNotNull(customerList);
        assertFalse(customerList.isEmpty());
        assertEquals(customerList.size(), maxDocuments);

        for (Customer customer : customerList) {
            System.out.println(customer);
            assertNotNull(customer);
            assertEquals(Const.FIRST_NAME, customer.getFirstName());
            assertEquals(Const.LAST_NAME, customer.getLastName());
        }
    }
    
    @AfterClass
    public static void after() {
        System.out.println(SortDescendingOperationTest.class.getName() + " ending.");
        assertNotNull(scope);
        assertNotNull(repository);
        assertTrue(repository.deleteAll());
        assertNotNull(manager);
        assertTrue(manager.close());
    }
}