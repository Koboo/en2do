package eu.koboo.en2do.test;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class RepositoryTest<E, ID, R extends Repository<E, ID>> {

    MongoManager manager;
    R repository;

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
