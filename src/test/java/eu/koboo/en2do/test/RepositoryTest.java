package eu.koboo.en2do.test;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class RepositoryTest<E, ID, R extends Repository<E, ID>> {

    MongoManager manager;
    R repository;

    @BeforeAll
    public void setup() {
        log.info("Starting Unit-Test [" + getClass().getName() + "]");
        SettingsBuilder settingsBuilder = new SettingsBuilder();

        manager = new MongoManager(settingsBuilder);
        assertNotNull(manager);
        repository = manager.create(repositoryClass());
        assertNotNull(repository);
        assertTrue(repository.drop());
        assertEquals(0, repository.findAll().size());
    }

    @AfterAll
    public void finish() {
        log.info("Stopping Unit-Test [" + getClass().getName() + "]");
        assertNotNull(manager);
        assertNotNull(repository);
        assertTrue(repository.drop());
        manager.close();
    }

    public abstract Class<R> repositoryClass();
}
