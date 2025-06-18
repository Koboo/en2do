package eu.koboo.en2do.test.annotations;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.test.AnnotationTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class TestEntityTest extends AnnotationTest<TestEntity, UUID, TestEntityRepository> {

    @Override
    public Class<TestEntityRepository> repositoryClass() {
        return TestEntityRepository.class;
    }

    @Test
    public void testTransientAnnotation() {
        TestEntity testEntity = new TestEntity();
        UUID entityId = UUID.randomUUID();
        testEntity.setUniqueId(entityId);
        testEntity.setSomeLong(10_000);
        assertTrue(repository.save(testEntity));

        Document document = collection.find(Filters.eq("_id", entityId)).first();
        assertNotNull(document);
        assertEquals(entityId, document.get("_id"));
        assertFalse(document.containsKey("someLong"));

        testEntity = repository.findFirstById(entityId);
        assertNotNull(testEntity);
        assertEquals(entityId, testEntity.getUniqueId());
        assertEquals(0, testEntity.getSomeLong());
    }
}