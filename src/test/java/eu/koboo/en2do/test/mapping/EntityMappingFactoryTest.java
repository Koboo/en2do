package eu.koboo.en2do.test.mapping;

import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.EntityMappingFactory;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EntityMappingFactoryTest {

    @Test
    void resolvesInheritedFieldsAndUsesOneMappingForEachField() throws ReflectiveOperationException {
        EntityMapping<Customer> mapping = EntityMappingFactory.create(Customer.class);

        assertEquals(Customer.class, mapping.getEntityClass());
        assertEquals(Set.of("id", "displayName", "tags"), mapping.getFieldsByJavaName().keySet());
        assertEquals(Set.of("_id", "display_name", "tags"), mapping.getFieldsByBsonName().keySet());
        assertSame(mapping.getIdField(), mapping.findByJavaName("id"));
        assertSame(mapping.getIdField(), mapping.findByBsonName("_id"));
        assertSame(mapping.findByJavaName("displayName"), mapping.findByBsonName("display_name"));
        assertEquals("customer-1", mapping.getIdField().getField().get(new Customer()));

        FieldMapping tags = mapping.findByJavaName("tags");
        assertEquals(List.class, tags.getType());
        assertEquals(Customer.class.getDeclaredField("tags").getGenericType(), tags.getGenericType());
        assertNotNull(mapping.getIdField().getAnnotation(Id.class));
        assertNull(mapping.findByBsonName("id"));
    }

    @Test
    void excludesStaticAndTransientFieldsBeforeValidation() {
        EntityMapping<ExcludedFields> mapping = EntityMappingFactory.create(ExcludedFields.class);

        assertEquals(Set.of("value"), mapping.getFieldsByJavaName().keySet());
        assertNull(mapping.getIdField());
    }

    @Test
    void excludesSyntheticEnclosingInstanceField() {
        EntityMapping<InnerEntity> mapping = EntityMappingFactory.create(InnerEntity.class);

        assertEquals(Set.of("value"), mapping.getFieldsByJavaName().keySet());
    }

    @Test
    void allowsEmbeddedObjectsWithoutAnId() {
        EntityMapping<EmbeddedObject> mapping = EntityMappingFactory.create(EmbeddedObject.class);

        assertNull(mapping.getIdField());
        assertEquals(Set.of("value"), mapping.getFieldsByBsonName().keySet());
    }

    @Test
    void exposesUnmodifiableLookups() {
        EntityMapping<Customer> mapping = EntityMappingFactory.create(Customer.class);

        assertThrows(UnsupportedOperationException.class, () -> mapping.getFieldsByJavaName().clear());
        assertThrows(UnsupportedOperationException.class, () -> mapping.getFieldsByBsonName().clear());
    }

    @Test
    void reusesMappingForTheSameEntityClass() {
        EntityMapping<Customer> firstMapping = EntityMappingFactory.create(Customer.class);
        EntityMapping<Customer> secondMapping = EntityMappingFactory.create(Customer.class);

        assertSame(firstMapping, secondMapping);
    }

    @Test
    void idUsesMongoNameEvenWhenTransformed() {
        EntityMapping<TransformedId> mapping = EntityMappingFactory.create(TransformedId.class);

        assertEquals("_id", mapping.getIdField().getBsonName());
        assertNull(mapping.findByBsonName("custom_id"));
    }

    @Test
    void rejectsBlankTransformedNamesWithFieldContext() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> EntityMappingFactory.create(BlankTransform.class));

        assertTrue(exception.getMessage().contains("@TransformField"));
        assertTrue(exception.getMessage().contains(BlankTransform.class.getName()));
        assertTrue(exception.getMessage().contains("value"));
    }

    @Test
    void rejectsHiddenAndCaseInsensitiveDuplicateJavaNames() {
        assertThrows(IllegalArgumentException.class, () -> EntityMappingFactory.create(HiddenField.class));
        assertThrows(IllegalArgumentException.class, () -> EntityMappingFactory.create(CaseDuplicate.class));
    }

    @Test
    void rejectsDuplicateBsonNamesAndIdCollisions() {
        assertThrows(IllegalArgumentException.class, () -> EntityMappingFactory.create(DuplicateBson.class));
        assertThrows(IllegalArgumentException.class, () -> EntityMappingFactory.create(IdCollision.class));
    }

    @Test
    void rejectsMultipleIdsAcrossInheritance() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> EntityMappingFactory.create(MultipleIds.class));

        assertTrue(exception.getMessage().contains("Multiple @Id fields"));
    }

    @Test
    void rejectsPersistedFinalFields() {
        assertThrows(IllegalArgumentException.class, () -> EntityMappingFactory.create(FinalField.class));
    }

    private static class BaseEntity {
        @Id
        private String id = "customer-1";
    }

    private static class Customer extends BaseEntity {
        @TransformField("display_name")
        private String displayName;
        private List<String> tags;
    }

    private static class ExcludedFields {
        @Id
        private static final String CONSTANT = "ignored";
        @Id
        private transient String temporary;
        @Transient
        @TransformField("")
        private final String ignored = "ignored";
        private String value;
    }

    private class InnerEntity {
        private String value;
    }

    private static class EmbeddedObject {
        private String value;
    }

    private static class TransformedId {
        @Id
        @TransformField("custom_id")
        private String id;
    }

    private static class BlankTransform {
        @TransformField(" \t ")
        private String value;
    }

    private static class HiddenField extends EmbeddedObject {
        private String value;
    }

    private static class CaseDuplicate extends EmbeddedObject {
        private String VALUE;
    }

    private static class DuplicateBson {
        @TransformField("stored_name")
        private String first;
        @TransformField("STORED_NAME")
        private String second;
    }

    private static class IdCollision extends BaseEntity {
        @TransformField("_id")
        private String value;
    }

    private static class MultipleIds extends BaseEntity {
        @Id
        private String secondId;
    }

    private static class FinalField {
        private final String value = "persisted";
    }
}
