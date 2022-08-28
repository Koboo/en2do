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

import static org.junit.Assert.*;

public class AsyncOperationSubscribeTest {

    static MongoManager manager;
    static CustomerRepository repository;
    static CustomerScope scope;

    @BeforeClass
    public static void before() {
        System.out.println(AsyncOperationSubscribeTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = new CustomerRepository(manager);
        assertNotNull(repository);
        scope = new CustomerScope(repository);
        assertNotNull(scope);
    }

    @Test
    public void operationTest() throws Exception {
        Bson idFilter = scope.eq(Customer::getUniqueId, Const.UNIQUE_ID);
        assertNotNull(idFilter);

        Result<Boolean> deleteResult = repository.deleteAllAsync();
        assertNotNull(deleteResult);
        deleteResult.subscribe(deleted -> {
            assertTrue(deleted);

            Customer original = Const.createNew();
            assertNotNull(original);

            Result<Boolean> saveResult = repository.saveAsync(original);
            assertNotNull(saveResult);
            saveResult.subscribe(saved -> {
                assertTrue(saved);

                Result<Boolean> existsResult = repository.existsAsync(idFilter);
                assertNotNull(existsResult);
                existsResult.subscribe(exists -> {
                    assertTrue(exists);

                    Result<Customer> result = repository.findAsync(idFilter);
                    assertNotNull(result);

                    result.subscribe(customer -> {
                        assertNotNull(customer);
                        assertEquals(Const.UNIQUE_ID, customer.getUniqueId());
                        assertEquals(Const.FIRST_NAME, customer.getFirstName());
                        assertEquals(Const.LAST_NAME, customer.getLastName());
                        assertEquals(Const.BIRTHDAY, customer.getBirthday());
                    });
                });
            });
        });

        Thread.sleep(1000L);
    }

    @AfterClass
    public static void after() {
        System.out.println(AsyncOperationSubscribeTest.class.getName() + " ending.");
        assertTrue(repository.deleteAllAsync().await());
        assertTrue(manager.close());
    }
}