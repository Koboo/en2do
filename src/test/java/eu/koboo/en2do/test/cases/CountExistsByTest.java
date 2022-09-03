package eu.koboo.en2do.test.cases;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerRepository;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CountExistsByTest {

    static MongoManager manager;
    static CustomerRepository repository;

    @BeforeAll
    public static void setup() {
        System.out.println(CountExistsByTest.class.getName() + " START");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(CustomerRepository.class);
        assertNotNull(repository);
    }

    @Test
    @Order(1)
    public void cleanUpRepository() {
        assertTrue(repository.deleteAll());
        List<Customer> customerList = repository.findAll();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        for(int i = 0; i < 30; i++) {
            Customer customer = Const.createNew();
            customer.setUniqueId(UUID.randomUUID());
            customer.setCustomerId(i);
            assertTrue(repository.save(customer));
            assertTrue(repository.exists(customer));
        }
    }

    @Test
    @Order(3)
    public void countCustomer() {
        long count = repository.countByFirstName("Rainer");
        assertEquals(30, count);
    }

    @Test
    @Order(4)
    public void existsCustomer() {
        boolean exists1 = repository.existsByLastName("Zufall");
        assertTrue(exists1);
        boolean exists2 = repository.existsByLastNameContains("fal");
        assertTrue(exists2);
    }

    @AfterAll
    public static void finish() {
        System.out.println(CountExistsByTest.class.getName() + " END");
        assertTrue(repository.deleteAll());
        assertTrue(manager.close());
    }
}