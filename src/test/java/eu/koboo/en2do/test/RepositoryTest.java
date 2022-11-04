package eu.koboo.en2do.test;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.Repository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class RepositoryTest<E, ID, R extends Repository<E, ID>> {

    protected MongoManager manager;
    protected R repository;

    @BeforeAll
    public void setup() {
        log.info("Starting Unit-Test [" + getClass().getName() + "]");
        manager = new MongoManager();
        assertNotNull(manager);
        repository = manager.create(repositoryClass());
        assertNotNull(repository);
    }

    @AfterAll
    public void finish() {
        log.info("Stopping Unit-Test [" + getClass().getName() + "]");
        assertTrue(repository.drop());
        assertTrue(manager.close());
    }

    public abstract Class<R> repositoryClass();
}
