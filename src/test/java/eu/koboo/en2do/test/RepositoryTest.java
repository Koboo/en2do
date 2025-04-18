package eu.koboo.en2do.test;

import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class RepositoryTest<E, ID, R extends Repository<E, ID>> {

    R repository;

    @BeforeAll
    public void setup() {
        log.info("Starting Unit-Test: {}", getClass().getSimpleName());
        repository = TestMongoManager.MANAGER.create(repositoryClass());
        assertNotNull(repository);
        assertTrue(repository.deleteAll());
        assertEquals(0, repository.countAll());
    }

    @AfterAll
    public void finish() {
        log.info("Stopping Unit-Test: {}", getClass().getSimpleName());
        assertNotNull(repository);
        assertTrue(repository.drop());
        assertEquals(0, repository.countAll());
    }

    public abstract Class<R> repositoryClass();
}
