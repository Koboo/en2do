package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class SortDescendingTest {

    static int documentsCount = 20;
    static int maxDocuments = 10;
    static MongoManager manager;
    static CustomerRepository repository;

    @BeforeClass
    public static void before() {
        System.out.println(SortDescendingTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(CustomerRepository.class);
        assertNotNull(repository);
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

        /*
        TODO: Create sorting with limit
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

         */
    }
    
    @AfterClass
    public static void after() {
        System.out.println(SortDescendingTest.class.getName() + " ending.");
        assertNotNull(repository);
        assertTrue(repository.deleteAll());
        assertNotNull(manager);
        assertTrue(manager.close());
    }
}