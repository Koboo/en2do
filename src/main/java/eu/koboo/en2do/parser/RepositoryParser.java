package eu.koboo.en2do.parser;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.parser.indices.CompoundIndicesParser;
import eu.koboo.en2do.parser.indices.GeoIndicesParser;
import eu.koboo.en2do.parser.indices.IndicesParser;
import eu.koboo.en2do.parser.indices.TimeToLiveIndicesParser;
import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import eu.koboo.en2do.utility.Tuple;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepositoryParser {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    SettingsBuilder builder;
    Map<Class<?>, Set<Field>> reflectedFieldIndex;
    Set<IndicesParser> indicesParserRegistry;

    public RepositoryParser(SettingsBuilder builder) {
        this.builder = builder;
        this.reflectedFieldIndex = new ConcurrentHashMap<>();
        this.indicesParserRegistry = new LinkedHashSet<>();

        this.indicesParserRegistry.add(new CompoundIndicesParser());
        this.indicesParserRegistry.add(new TimeToLiveIndicesParser());
        this.indicesParserRegistry.add(new GeoIndicesParser());
    }

    public void destroy() {
        for (Set<Field> value : reflectedFieldIndex.values()) {
            value.clear();
        }
        reflectedFieldIndex.clear();
        indicesParserRegistry.clear();
    }

    public Tuple<Class<?>, Class<?>> parseGenericTypes(Class<?> repositoryClass, Class<?> typeClass) throws Exception {
        Map<Class<?>, List<Class<?>>> genericTypes = GenericUtils.getGenericTypes(repositoryClass);
        if (genericTypes.isEmpty()) {
            throw new RepositoryNoTypeException(repositoryClass);
        }

        List<Class<?>> classList = genericTypes.get(typeClass);
        if (classList == null || classList.size() != 2) {
            throw new RepositoryEntityNotFoundException(repositoryClass);
        }

        Class<?> entityClass = classList.get(0);
        Class<?> entityIdClass = classList.get(1);
        return new Tuple<>(entityClass, entityIdClass);
    }

    public void validateRepositoryTypes(Class<?> repositoryClass, Tuple<Class<?>, Class<?>> entityTypes) throws Exception {
        // Doesn't implement async repository, so we can ignore that.
        if (!AsyncRepository.class.isAssignableFrom(repositoryClass)) {
            return;
        }
        Tuple<Class<?>, Class<?>> asyncTypes = parseGenericTypes(repositoryClass, AsyncRepository.class);
        Class<?> asyncEntityType = asyncTypes.getFirst();
        Class<?> entityType = entityTypes.getFirst();
        if (GenericUtils.isNotTypeOf(asyncEntityType, entityType)) {
            throw new RepositoryInvalidTypeException(entityType, asyncEntityType, repositoryClass);
        }
        Class<?> asyncEntityId = asyncTypes.getSecond();
        Class<?> entityId = entityTypes.getSecond();
        if (GenericUtils.isNotTypeOf(asyncEntityId, entityId)) {
            throw new RepositoryInvalidTypeException(entityId, asyncEntityId, repositoryClass);
        }
    }

    public String parseCollectionName(Class<?> repositoryClass, Class<?> entityClass) throws Exception {

        // Parse annotated collection name and create pojo-related mongo collection
        Collection collectionAnnotation = findCollectionAnnotation(repositoryClass, entityClass);
        if (collectionAnnotation == null) {
            throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
        }

        // Check if the collection name is valid and for duplication issues
        String entityCollectionName = collectionAnnotation.value();
        if (entityCollectionName.trim().equalsIgnoreCase("")) {
            throw new RepositoryInvalidNameException(repositoryClass, Collection.class, entityCollectionName);
        }

        String collectionPrefix = builder.getCollectionPrefix();
        if (collectionPrefix != null) {
            entityCollectionName = collectionPrefix + entityCollectionName;
        }

        String collectionSuffix = builder.getCollectionSuffix();
        if (collectionSuffix != null) {
            entityCollectionName = entityCollectionName + collectionSuffix;
        }

        if (!COLLECTION_REGEX_NAME.matcher(entityCollectionName).matches()) {
            throw new RepositoryInvalidNameException(repositoryClass, Collection.class, entityCollectionName);
        }

        return entityCollectionName;
    }

    private Collection findCollectionAnnotation(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            Collection collectionAnnotation = clazz.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                continue;
            }
            return collectionAnnotation;
        }
        return null;
    }

    public Set<Field> parseEntityFields(Class<?> entityClass) {
        Set<Field> entityFieldSet = reflectedFieldIndex.get(entityClass);
        if (entityFieldSet != null) {
            return entityFieldSet;
        }
        entityFieldSet = FieldUtils.collectFields(entityClass);
        reflectedFieldIndex.put(entityClass, entityFieldSet);
        return entityFieldSet;
    }

    public Field parseEntityIdField(Class<?> entityClass) {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);
        // Get the field of the uniqueId of the entity.
        for (Field field : entityFieldSet) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            // Check for @Id annotation to find unique identifier of entity
            if (!field.isAnnotationPresent(Id.class)) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        return null;
    }

    public Map<String, Field> parseSortedFieldBsonNames(Class<?> entityClass) {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);

        // Create list of all entity fields with their
        // respective bson names and related them to the Field object.
        List<String> fieldBsonList = new LinkedList<>();
        Map<String, Field> unsortedFieldMap = new HashMap<>();
        for (Field field : entityFieldSet) {
            String bsonName = FieldUtils.parseBsonName(field);
            fieldBsonList.add(bsonName);
            unsortedFieldMap.put(bsonName, field);
        }

        // Sorting the list by length and putting them back into a linked map.
        fieldBsonList.sort(Comparator.comparingInt(String::length));
        Collections.reverse(fieldBsonList);
        Map<String, Field> sortedFieldMap = new LinkedHashMap<>();
        for (String bsonName : fieldBsonList) {
            sortedFieldMap.put(bsonName, unsortedFieldMap.get(bsonName));
        }

        fieldBsonList.clear();
        unsortedFieldMap.clear();

        return sortedFieldMap;
    }

    public void parseIndices(Class<?> repositoryClass, Class<?> entityClass, MongoCollection<?> collection)
        throws Exception {
        Set<Field> entityFieldSet = parseEntityFields(entityClass);
        for (IndicesParser indicesParser : indicesParserRegistry) {
            indicesParser.parse(repositoryClass, entityClass, collection, entityFieldSet);
        }
    }
}
