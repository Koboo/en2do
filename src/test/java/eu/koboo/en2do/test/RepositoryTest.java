package eu.koboo.en2do.test;

import com.mongodb.MongoCompressor;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.configurators.ClientConfiguratorCompressors;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

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
        log.info("Starting Unit-Test: " + getClass().getSimpleName());
        SettingsBuilder settingsBuilder = new SettingsBuilder()
            .clientConfigurator(new ClientConfiguratorCompressors(Collections.singletonList(MongoCompressor.createZlibCompressor())))
            .appendMethodNameAsQueryComment()
            .disableMongoDBLogger();

        manager = new MongoManager(settingsBuilder);
        assertNotNull(manager);
        repository = manager.create(repositoryClass());
        assertNotNull(repository);
        List<E> all = repository.findAll();
        assertNotNull(all);
        assertTrue(repository.deleteMany(all));
        assertEquals(0, repository.findAll().size());
    }

    @AfterAll
    public void finish() {
        log.info("Stopping Unit-Test: " + getClass().getSimpleName());
        assertNotNull(manager);
        assertNotNull(repository);
//        assertTrue(repository.drop());
        manager.close();
    }

    public abstract Class<R> repositoryClass();
}
