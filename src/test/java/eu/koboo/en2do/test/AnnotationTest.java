package eu.koboo.en2do.test;


import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AnnotationTest<E, ID, R extends Repository<E, ID>> extends RepositoryTest<E, ID, R> {

    MongoCollection<Document> collection;

    @BeforeAll
    public void setup() {
        super.setup();
        String collectionName = repository.getCollectionName();
        assertNotNull(collectionName);
        assertFalse(collectionName.isEmpty());
        collection = TestMongoManager.MANAGER.getMongoDatabase().getCollection(collectionName);
        assertNotNull(collection);
    }

    public abstract Class<R> repositoryClass();
}
