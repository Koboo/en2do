package eu.koboo.en2do.test.mapping;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.geojson.Point;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;
import eu.koboo.en2do.mongodb.exception.methods.MethodFieldNotFoundException;
import eu.koboo.en2do.mongodb.exception.methods.MethodMismatchingTypeException;
import eu.koboo.en2do.mongodb.exception.repository.RepositorySetterNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdMismatchException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIndexCompoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIndexTTLException;
import eu.koboo.en2do.mongodb.indexer.MethodIndexer;
import eu.koboo.en2do.mongodb.indexer.RepositoryIndexer;
import eu.koboo.en2do.mongodb.indexparser.IndexParser;
import eu.koboo.en2do.mongodb.mapping.EntityMappingFactory;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.GeoIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
import eu.koboo.en2do.repository.methods.transform.NestedBsonKey;
import eu.koboo.en2do.test.TestMongoManager;
import eu.koboo.en2do.test.annotations.TestEntityRepository;
import eu.koboo.en2do.test.customer.CustomerRepository;
import eu.koboo.en2do.test.customerextended.CustomerExtendedRepository;
import eu.koboo.en2do.test.generic.GenericModelRepository;
import eu.koboo.en2do.test.geotest.GeoEntityRepository;
import eu.koboo.en2do.test.mapkeys.MapKeyRepository;
import eu.koboo.en2do.test.user.UserRepository;
import eu.koboo.en2do.utility.reflection.FieldUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.Document;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EntityMappingIntegrationTest {

    @Test
    void sharesMappingAndResolvesInheritedPrimitiveId() {
        RepositoryIndexer<MappedEntity, Integer, MappedRepository> indexer = indexer(MappedRepository.class);
        RepositoryData<MappedEntity, Integer, MappedRepository> data = indexer.index(null);

        assertSame(indexer.getEntityMapping(), data.getEntityMapping());
        assertEquals(MappedEntity.class, data.getEntityClass());
        assertEquals(Integer.class, data.getEntityUniqueIdClass());
        assertSame(indexer.getEntityMapping().getIdField().getField(), data.getEntityUniqueIdField());
        assertEquals(BaseEntity.class, data.getEntityUniqueIdField().getDeclaringClass());
    }

    @Test
    void buildsQueriesUsingResolvedNamesAndLongestJavaName() {
        RepositoryData<MappedEntity, Integer, MappedRepository> data = indexMethods(MappedRepository.class);

        assertEquals(BsonDocument.parse("{_id: 7}"), filter(data, "countByEntityId", 7));
        assertEquals(BsonDocument.parse("{long_stored_name: 'short'}"), filter(data, "countByName", "short"));
        assertEquals(BsonDocument.parse("{n: 'long'}"), filter(data, "countByNameLong", "long"));
        assertEquals(BsonDocument.parse("{inheritedLabel: 'parent'}"),
            filter(data, "countByInheritedLabel", "parent"));
    }

    @Test
    void preservesExplicitBsonPathsEvenWhenTheyMatchTheJavaIdName() {
        RepositoryData<MappedEntity, Integer, MappedRepository> data = indexMethods(MappedRepository.class);

        assertEquals(BsonDocument.parse("{entityId: 'raw'}"), filter(data, "countByRawIdentifier", "raw"));
        assertEquals(BsonDocument.parse("{'embedded.value': 'nested'}"),
            filter(data, "countByEmbedded", "nested"));
    }

    @Test
    void rejectsQueriesOnExcludedFieldsAndWrongParameterTypes() {
        assertThrows(MethodFieldNotFoundException.class, () -> indexMethods(TransientQueryRepository.class));
        assertThrows(MethodFieldNotFoundException.class, () -> indexMethods(StaticQueryRepository.class));
        assertThrows(MethodMismatchingTypeException.class, () -> indexMethods(WrongParameterRepository.class));
    }

    @Test
    void validatesRepositoryIdPresenceAndType() {
        assertThrows(RepositoryTypeIdNotFoundException.class, () -> indexer(MissingIdRepository.class));
        assertThrows(RepositoryTypeIdMismatchException.class, () -> indexer(WrongIdRepository.class));
    }

    @Test
    void includesRepositoryContextForInvalidMappings() {
        RepositoryTypeException exception = assertThrows(RepositoryTypeException.class,
            () -> indexer(DuplicateFieldRepository.class));

        assertTrue(exception.getMessage().contains(DuplicateFieldRepository.class.getName()));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void managerCreatesUsableProxyAndCodecAgreesWithMapping() {
        ManagedRepository repository = TestMongoManager.MANAGER.create(ManagedRepository.class);
        MappedEntity entity = new MappedEntity();
        repository.setUniqueId(entity, 42);
        entity.setName("example");

        assertEquals(42, repository.getUniqueId(entity));
        assertEquals(MappedEntity.class, repository.getEntityClass());
        assertEquals(Integer.class, repository.getEntityUniqueIdClass());
        assertSame(repository, TestMongoManager.MANAGER.create(ManagedRepository.class));

        BsonDocument document = new BsonDocument();
        repository.getNativeCollection().getCodecRegistry().get(MappedEntity.class).encode(
            new BsonDocumentWriter(document), entity,
            EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        assertEquals(42, document.getInt32("_id").getValue());
        assertEquals("example", document.getString("long_stored_name").getValue());
        assertFalse(document.containsKey("legacy_id"));
        assertFalse(document.containsKey("ignored"));
    }

    @Test
    void createsCompoundTtlAndGeoIndexesUsingStoredNames() {
        List<BsonDocument> indexes = new ArrayList<>();
        List<IndexOptions> options = new ArrayList<>();
        new IndexParser().parseIndices(MappedRepository.class, EntityMappingFactory.create(IndexedEntity.class),
            captureIndexes(indexes, options));

        BsonDocument compound = BsonDocument.parse("{_id: 1, n: -1}");
        BsonDocument ttl = BsonDocument.parse("{expires_at: 1}");
        BsonDocument geo = BsonDocument.parse("{position: '2dsphere'}");
        assertEquals(Set.of(compound, ttl, geo), Set.copyOf(indexes));
        assertEquals(3, indexes.size());
        assertEquals(Boolean.TRUE, options.get(indexes.indexOf(compound)).isUnique());
        assertEquals(3L, options.get(indexes.indexOf(ttl)).getExpireAfter(TimeUnit.MINUTES));
    }

    @Test
    void rejectsIndexesOnExcludedFieldsAndInvalidTtlTypes() {
        List<BsonDocument> indexes = new ArrayList<>();
        MongoCollection<Object> collection = captureIndexes(indexes, new ArrayList<>());
        IndexParser parser = new IndexParser();

        assertThrows(RepositoryTypeIndexCompoundException.class, () -> parser.parseIndices(MappedRepository.class,
            EntityMappingFactory.create(ExcludedCompoundEntity.class), collection));
        assertThrows(RepositoryTypeIndexTTLException.class, () -> parser.parseIndices(MappedRepository.class,
            EntityMappingFactory.create(ExcludedTtlEntity.class), collection));
        assertThrows(RepositoryTypeIndexTTLException.class, () -> parser.parseIndices(MappedRepository.class,
            EntityMappingFactory.create(WrongTtlTypeEntity.class), collection));
        assertTrue(indexes.isEmpty());
    }

    @Test
    void compatibilityValidationDoesNotClearSharedReflectionCache() {
        Set<Field> cachedFields = FieldUtils.collectFields(EmbeddedEntity.class);
        Set<Field> originalFields = Set.copyOf(cachedFields);

        Validator.validateCompatibility(MongoClientSettings.getDefaultCodecRegistry(), MappedRepository.class,
            EntityMappingFactory.create(EmbeddedEntity.class));

        assertFalse(originalFields.isEmpty());
        assertEquals(originalFields, cachedFields);
    }

    @Test
    void validatesNestedMappingsWhileSkippingCodecBackedAndGenericFields() {
        assertDoesNotThrow(() -> Validator.validateCompatibility(MongoClientSettings.getDefaultCodecRegistry(),
            MappedRepository.class, EntityMappingFactory.create(EmbeddedContainer.class)));
    }

    @Test
    void stillValidatesAccessorsOfEmbeddedTypesWithoutCodecs() {
        assertThrows(RepositorySetterNotFoundException.class, () -> Validator.validateCompatibility(
            MongoClientSettings.getDefaultCodecRegistry(), MappedRepository.class,
            EntityMappingFactory.create(InvalidEmbeddedContainer.class)));
    }

    @Test
    void indexesExistingRepositoryDefinitionsWithoutDatabaseOperations() {
        assertDoesNotThrow(() -> indexMethods(CustomerRepository.class));
        assertDoesNotThrow(() -> indexMethods(CustomerExtendedRepository.class));
        assertDoesNotThrow(() -> indexMethods(GenericModelRepository.class));
        assertDoesNotThrow(() -> indexMethods(GeoEntityRepository.class));
        assertDoesNotThrow(() -> indexMethods(MapKeyRepository.class));
        assertDoesNotThrow(() -> indexMethods(TestEntityRepository.class));
        assertDoesNotThrow(() -> indexMethods(UserRepository.class));
    }

    private <E, ID, R extends Repository<E, ID>> RepositoryIndexer<E, ID, R> indexer(Class<R> repositoryClass) {
        return new RepositoryIndexer<>(TestMongoManager.MANAGER,
            TestMongoManager.MANAGER.getMongoDatabase().getCodecRegistry(),
            new PredefinedMethodRegistry(), repositoryClass);
    }

    private <E, ID, R extends Repository<E, ID>> RepositoryData<E, ID, R> indexMethods(Class<R> repositoryClass) {
        RepositoryIndexer<E, ID, R> indexer = indexer(repositoryClass);
        RepositoryData<E, ID, R> data = indexer.index(null);
        for (Method method : repositoryClass.getDeclaredMethods()) {
            new MethodIndexer<>(new PredefinedMethodRegistry(), indexer, method).indexMethod(data);
        }
        return data;
    }

    private BsonDocument filter(RepositoryData<?, ?, ?> data, String methodName, Object... arguments) {
        Bson filter = data.lookupDynamicMethod(methodName).createFilter(arguments);
        return filter.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    @SuppressWarnings("unchecked")
    private <E> MongoCollection<E> captureIndexes(List<BsonDocument> indexes, List<IndexOptions> options) {
        return (MongoCollection<E>) Proxy.newProxyInstance(MongoCollection.class.getClassLoader(),
            new Class<?>[]{MongoCollection.class}, (proxy, method, args) -> {
                if (method.getName().equals("createIndex")) {
                    indexes.add(((Bson) args[0]).toBsonDocument(Document.class,
                        MongoClientSettings.getDefaultCodecRegistry()));
                    options.add(args.length == 2 ? (IndexOptions) args[1] : new IndexOptions());
                    return "captured_index";
                }
                throw new AssertionError("Unexpected collection operation: " + method.getName());
            });
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BaseEntity {
        @Id
        @TransformField("legacy_id")
        int entityId;
        String inheritedLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MappedEntity extends BaseEntity {
        @TransformField("long_stored_name")
        String name;
        @TransformField("n")
        String nameLong;
        @Transient
        String ignored;
        static String constant;
    }

    @Collection("mapping_queries")
    public interface MappedRepository extends Repository<MappedEntity, Integer> {
        long countByEntityId(int id);
        long countByName(String name);
        long countByNameLong(String name);
        long countByInheritedLabel(String label);
        @NestedBsonKey(id = "RawIdentifier", bson = "entityId")
        long countByRawIdentifier(String value);
        @NestedBsonKey(id = "Embedded", bson = "embedded.value")
        long countByEmbedded(String value);
    }

    @Collection("mapping_managed")
    public interface ManagedRepository extends Repository<MappedEntity, Integer> {
    }

    @Collection("mapping_transient_query")
    public interface TransientQueryRepository extends Repository<MappedEntity, Integer> {
        long countByIgnored(String value);
    }

    @Collection("mapping_static_query")
    public interface StaticQueryRepository extends Repository<MappedEntity, Integer> {
        long countByConstant(String value);
    }

    @Collection("mapping_wrong_parameter")
    public interface WrongParameterRepository extends Repository<MappedEntity, Integer> {
        long countByNameLong(int value);
    }

    @Collection("mapping_missing_id")
    public interface MissingIdRepository extends Repository<EmbeddedEntity, String> {
    }

    @Collection("mapping_wrong_id")
    public interface WrongIdRepository extends Repository<MappedEntity, UUID> {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DuplicateFieldEntity extends MappedEntity {
        @TransformField("n")
        String otherName;
    }

    @Collection("mapping_duplicate_field")
    public interface DuplicateFieldRepository extends Repository<DuplicateFieldEntity, Integer> {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EmbeddedEntity {
        String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EmbeddedContainer {
        EmbeddedEntity nested;
        String text;
        Object arbitrary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InvalidEmbeddedContainer {
        MissingSetterEntity nested;
    }

    @Getter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MissingSetterEntity {
        String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @CompoundIndex(value = {@Index("entityId"), @Index(value = "NAMELONG", ascending = false)}, uniqueIndex = true)
    @TTLIndex(value = "EXPIRESAT", ttl = 3, time = TimeUnit.MINUTES)
    public static class IndexedEntity extends MappedEntity {
        @TransformField("expires_at")
        Date expiresAt;
        @GeoIndex
        @TransformField("position")
        Point location;
        @Transient
        @GeoIndex
        Point ignoredLocation;
    }

    @CompoundIndex({@Index("ignored")})
    public static class ExcludedCompoundEntity extends MappedEntity {
    }

    @TTLIndex("ignored")
    public static class ExcludedTtlEntity extends MappedEntity {
    }

    @TTLIndex("name")
    public static class WrongTtlTypeEntity extends MappedEntity {
    }
}
