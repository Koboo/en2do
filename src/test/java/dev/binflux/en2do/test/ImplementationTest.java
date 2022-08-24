package dev.binflux.en2do.test;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.Scope;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import dev.binflux.en2do.test.impl.CustomerRepository;
import dev.binflux.en2do.test.impl.CustomerScope;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class ImplementationTest {

    static MongoManager manager;
    static CustomerRepository repository;
    static Scope<Customer, UUID> scope;

    @BeforeClass
    public static void before() {
        System.out.println(ImplementationTest.class.getName() + " starting.");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = new CustomerRepository(manager);
        assertNotNull(repository);
        scope = new CustomerScope(repository);
        assertNotNull(scope);
    }

    @Test
    public void operationTest() {
        assertNotNull(Const.CUSTOMER);
        assertTrue(repository.save(Const.CUSTOMER));

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
        System.out.println(ImplementationTest.class.getName() + " ending.");
        assertTrue(repository.deleteAll());
        assertTrue(manager.close());
    }
}