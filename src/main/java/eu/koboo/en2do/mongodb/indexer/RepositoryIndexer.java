package eu.koboo.en2do.mongodb.indexer;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.exception.RepositoryException;
import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameDuplicateException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameInvalidException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdMismatchException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdNotFoundException;
import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.EntityMappingFactory;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.NameConvention;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.utility.Tuple;
import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.configuration.CodecRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RepositoryIndexer<E, ID, R extends Repository<E, ID>> {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    MongoManager mongoManager;
    SettingsBuilder settingsBuilder;
    CodecRegistry codecRegistry;
    PredefinedMethodRegistry predefinedMethodRegistry;
    Class<R> repositoryClass;
    EntityMapping<E> entityMapping;
    Class<ID> idClass;
    String collectionName;

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
        Class<E> entityClass = (Class<E>) repositoryGenericTypeTuple.getFirst();
        this.idClass = (Class<ID>) repositoryGenericTypeTuple.getSecond();

        try {
            this.entityMapping = EntityMappingFactory.create(entityClass);
        } catch (IllegalArgumentException e) {
            throw new RepositoryTypeException("Invalid entity mapping.", repositoryClass, entityClass, e);
        }
        if (entityMapping.getIdField() == null) {
            throw new RepositoryTypeIdNotFoundException(repositoryClass, entityClass);
        }
        Class<?> fieldIdClass = PrimitiveUtils.wrapperOf(entityMapping.getIdField().getType());
        if (!idClass.equals(fieldIdClass)) {
            throw new RepositoryTypeIdMismatchException(repositoryClass, entityClass, idClass, fieldIdClass);
        }

        this.collectionName = parseFullCollectionName();
        Validator.validateCompatibility(codecRegistry, repositoryClass, entityMapping);
    }

    public Class<E> getEntityClass() {
        return entityMapping.getEntityClass();
    }

    public Field getIdField() {
        return entityMapping.getIdField().getField();
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
        if (!(repositoryType instanceof ParameterizedType parameterizedType)) {
            throw new RepositoryException("Couldn't find parameterized types.", repositoryClass);
        }

        Class<?> entityTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> idTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[1];
        return new Tuple<>(entityTypeClass, idTypeClass);
    }

    private Collection parseCollectionAnnotation() {
        Collection collectionAnnotation = getEntityClass().getAnnotation(Collection.class);
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
        String annotatedCollectionName = collectionAnnotation.value();
        if (annotatedCollectionName.trim().equalsIgnoreCase("")) {
            return NameConvention.SNAKE_CASE.generate(repositoryClass);
        }
        return annotatedCollectionName;
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

    public RepositoryData<E, ID, R> index(MongoCollection<E> entityCollection) {
        return new RepositoryData<>(
            mongoManager,
            this,
            entityCollection
        );
    }
}
