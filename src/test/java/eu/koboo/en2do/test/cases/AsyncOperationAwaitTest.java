package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.Result;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import eu.koboo.en2do.test.customer.CustomerScope;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class AsyncOperationAwaitTest {

    static MongoManager manager;
    static CustomerRepository repository;
    static CustomerScope scope;
    static ExecutorService executorService;

    @BeforeClass
    public static void before() {
        System.out.println(AsyncOperationAwaitTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = new CustomerRepository(manager);
        assertNotNull(repository);
        scope = new CustomerScope(repository);
        executorService = Executors.newSingleThreadExecutor();
        assertNotNull(executorService);
    }

    @Test
    public void operationTest() {
        Result<Boolean> deleteResult = repository.deleteAllAsync();
        assertNotNull(deleteResult);
        assertTrue(deleteResult.await());

        Customer original = Const.createNew();
        assertNotNull(original);

        Result<Boolean> saveResult = repository.saveAsync(original);
        assertNotNull(saveResult);
        assertTrue(saveResult.await());

        Bson idFilter = scope.eq(Customer::getUniqueId, Const.UNIQUE_ID);
        assertNotNull(idFilter);
        Result<Boolean> existsResult = repository.existsAsync(idFilter);
        assertNotNull(existsResult);
        assertTrue(existsResult.await());

        Result<Customer> result = repository.findAsync(idFilter);
        assertNotNull(result);

        Customer customer = result.await();
        assertNotNull(customer);
        assertEquals(Const.UNIQUE_ID, customer.getUniqueId());
        assertEquals(Const.FIRST_NAME, customer.getFirstName());
        assertEquals(Const.LAST_NAME, customer.getLastName());
        assertEquals(Const.BIRTHDAY, customer.getBirthday());
    }

    @AfterClass
    public static void after() {
        System.out.println(AsyncOperationAwaitTest.class.getName() + " ending.");
        executorService.shutdown();
        assertTrue(repository.deleteAllAsync().await());
        assertTrue(manager.close());
    }
}