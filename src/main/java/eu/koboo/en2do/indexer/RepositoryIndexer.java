package eu.koboo.en2do.indexer;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.exception.RepositoryException;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.NameConvention;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.Transient;
import eu.koboo.en2do.utility.Tuple;
import eu.koboo.en2do.utility.parse.ParseUtils;
import eu.koboo.en2do.utility.reflection.FieldUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.configuration.CodecRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepositoryIndexer<E, ID, R extends Repository<E, ID>> {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    MongoManager mongoManager;
    SettingsBuilder settingsBuilder;
    CodecRegistry codecRegistry;
    PredefinedMethodRegistry predefinedMethodRegistry;
    Class<R> repositoryClass;
    Class<E> entityClass;
    Class<ID> idClass;
    String collectionName;

    Map<String, Field> bsonToFieldMap;

    Field idField;

    @SuppressWarnings("unchecked")
    public RepositoryIndexer(MongoManager mongoManager,
                             CodecRegistry codecRegistry,
                             PredefinedMethodRegistry predefinedMethodRegistry,
                             Class<R> repositoryClass) {
        this.mongoManager = mongoManager;
        this.settingsBuilder = mongoManager.getSettingsBuilder();
        this.codecRegistry = codecRegistry;
        this.predefinedMethodRegistry = predefinedMethodRegistry;
        this.repositoryClass = repositoryClass;

        Tuple<Class<?>, Class<?>> repositoryGenericTypeTuple = parseGenericTypes();
        this.entityClass = (Class<E>) repositoryGenericTypeTuple.getFirst();
        this.idClass = (Class<ID>) repositoryGenericTypeTuple.getSecond();

        this.collectionName = parseFullCollectionName();

        this.bsonToFieldMap = parseBsonToFieldMapLengthSorted();

        this.idField = parseIdField();

        Validator.validateCompatibility(codecRegistry, repositoryClass, entityClass);
    }

    private Tuple<Class<?>, Class<?>> parseGenericTypes() {
        if (!Repository.class.isAssignableFrom(repositoryClass)) {
            throw new RepositoryException("Couldn't Repository interface.", repositoryClass);
        }

        Type[] genericInterfaces;
        try {
            genericInterfaces = repositoryClass.getGenericInterfaces();
        } catch (Exception e) {
            throw new RepositoryException("An exception occurred while resolving generic types.", repositoryClass, e);
        }

        Type repositoryType = genericInterfaces[0];
        if (!(repositoryType instanceof ParameterizedType)) {
            throw new RepositoryException("Couldn't find parameterized types.", repositoryClass);
        }
        ParameterizedType parameterizedType = (ParameterizedType) repositoryType;

        Class<?> entityTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> idTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[1];
        return new Tuple<>(entityTypeClass, idTypeClass);
    }

    private Collection parseCollectionAnnotation() {
        Collection collectionAnnotation = entityClass.getAnnotation(Collection.class);
        if (collectionAnnotation != null) {
            return collectionAnnotation;
        }
        collectionAnnotation = repositoryClass.getAnnotation(Collection.class);
        if (collectionAnnotation != null) {
            return collectionAnnotation;
        }
        throw new RepositoryNameNotFoundException(repositoryClass);
    }

    private String parseBaseCollectionName() {
        NameConvention convention = settingsBuilder.getCollectionNameConvention();
        if (convention != null) {
            return convention.generate(repositoryClass);
        }

        // Parse annotated collection name and create pojo-related mongo collection
        Collection collectionAnnotation = parseCollectionAnnotation();

        // Check if the collection name is valid and for duplication issues
        String baseCollectionName = collectionAnnotation.value();
        if (baseCollectionName.trim().equalsIgnoreCase("")) {
            throw new RepositoryNameEmptyException(repositoryClass, baseCollectionName);
        }
        return baseCollectionName;
    }

    private String parseFullCollectionName() {
        String parsedCollectionName = parseBaseCollectionName();

        String prefix = settingsBuilder.getCollectionPrefix();
        if (prefix != null && !prefix.trim().equalsIgnoreCase("")) {
            parsedCollectionName = prefix + parsedCollectionName;
        }

        String suffix = settingsBuilder.getCollectionSuffix();
        if (suffix != null && !suffix.trim().equalsIgnoreCase("")) {
            parsedCollectionName = parsedCollectionName + suffix;
        }

        if (!COLLECTION_REGEX_NAME.matcher(parsedCollectionName).matches()) {
            throw new RepositoryNameInvalidException(repositoryClass,
                COLLECTION_REGEX_NAME.pattern(), parsedCollectionName);
        }

        for (Repository<?, ?> indexedRepository : mongoManager.getAllRepositories()) {
            if (!indexedRepository.getCollectionName().equalsIgnoreCase(parsedCollectionName)) {
                continue;
            }
            throw new RepositoryNameDuplicateException(repositoryClass, parsedCollectionName);
        }

        return parsedCollectionName;
    }

    private Map<String, Field> parseBsonToFieldMapLengthSorted() {
        Set<Field> entityFieldSet = FieldUtils.collectFields(entityClass);

        // Create list of all entity fields with their
        // respective bson names and related them to the Field object.
        List<String> fieldBsonList = new LinkedList<>();
        Map<String, Field> unsortedFieldMap = new HashMap<>();
        for (Field field : entityFieldSet) {
            String bsonName = ParseUtils.parseBsonName(field);
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

    private Field parseIdField() {
        // Get the field of the uniqueId of the entity.
        for (Field field : bsonToFieldMap.values()) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers)
                || Modifier.isStatic(modifiers)
                || Modifier.isTransient(modifiers)
                || field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            // Check for @Id annotation to find unique identifier of entity
            if (!field.isAnnotationPresent(Id.class)) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        throw new RepositoryTypeIdNotFoundException(repositoryClass, entityClass);
    }
}
