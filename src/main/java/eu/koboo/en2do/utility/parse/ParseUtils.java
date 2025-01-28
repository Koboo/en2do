package eu.koboo.en2do.utility.parse;

import com.mongodb.ConnectionString;
import eu.koboo.en2do.SettingsBuilder;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameEmptyException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameInvalidException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameNotFoundException;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.NameConvention;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.utility.Tuple;
import eu.koboo.en2do.utility.reflection.FieldUtils;
import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@UtilityClass
public class ParseUtils {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    public SettingsBuilder parseSettingsBuilder(SettingsBuilder settingsBuilder) {
        if (settingsBuilder == null) {
            return new SettingsBuilder();
        }
        return settingsBuilder;
    }

    public ExecutorService parseExecutorService(ExecutorService executorService) {
        if (executorService == null) {
            int defaultThreadAmount = Runtime.getRuntime().availableProcessors() * 2;
            return Executors.newFixedThreadPool(defaultThreadAmount);
        }
        return executorService;
    }

    public ConnectionString parseConnectionString(String connectString) {
        if (connectString != null && !connectString.isEmpty()) {
            return new ConnectionString(connectString);
        }
        // If no connection string is given, try loading it from the default sources e.g.:
        //  - Resource files,
        //  - System properties
        //  - Environmental variables.
        // See the Credentials class for information about the key of the property.
        // The property can differ in any string source.

        connectString = ConnectionStringUtils.fromFile();
        if (connectString != null && !connectString.isEmpty()) {
            return new ConnectionString(connectString);
        }

        connectString = ConnectionStringUtils.fromResource();
        if (connectString != null && !connectString.isEmpty()) {
            return new ConnectionString(connectString);
        }

        connectString = ConnectionStringUtils.fromSystemProperties();
        if (connectString != null && !connectString.isEmpty()) {
            return new ConnectionString(connectString);
        }

        connectString = ConnectionStringUtils.fromSystemEnvVars();
        if (connectString != null && !connectString.isEmpty()) {
            return new ConnectionString(connectString);
        }
        throw new IllegalStateException("Could not resolve any connection string to connect to.");
    }

    public Tuple<Class<?>, Class<?>> parseGenericTypes(Class<?> repositoryClass) {
        if (!Repository.class.isAssignableFrom(repositoryClass)) {
            throw new IllegalArgumentException("Couldn't Repository interface on class: " + repositoryClass.getName());
        }

        Type[] genericInterfaces;
        try {
            genericInterfaces = repositoryClass.getGenericInterfaces();
            if (genericInterfaces.length == 0) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't find any interface on class: " + repositoryClass.getName());
        }

        Type repositoryType = genericInterfaces[0];
        if (!(repositoryType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Couldn't find parameterized types of Repository " +
                "interface on class: " + repositoryClass.getName());
        }
        ParameterizedType parameterizedType = (ParameterizedType) repositoryType;

        Class<?> entityTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> idTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[1];
        return new Tuple<>(entityTypeClass, idTypeClass);
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

    private String parseBaseCollectionName(SettingsBuilder settingsBuilder,
                                 Class<?> repositoryClass, Class<?> entityClass) throws Exception {
        // Parse annotated collection name and create pojo-related mongo collection
        Collection collectionAnnotation = findCollectionAnnotation(repositoryClass, entityClass);
        if (collectionAnnotation == null) {
            throw new RepositoryNameNotFoundException(repositoryClass);
        }

        NameConvention convention = settingsBuilder.getCollectionNameConvention();
        if(convention != null) {
            return convention.generate(repositoryClass);
        }

        // Check if the collection name is valid and for duplication issues
        String entityCollectionName = collectionAnnotation.value();
        if (entityCollectionName.trim().equalsIgnoreCase("")) {
            throw new RepositoryNameEmptyException(repositoryClass, entityCollectionName);
        }
        return entityCollectionName;
    }

    public String parseCollectionName(SettingsBuilder settingsBuilder,
                                      Class<?> repositoryClass, Class<?> entityClass) throws Exception {
        String collectionBaseName = parseBaseCollectionName(settingsBuilder, repositoryClass, entityClass);

        String collectionPrefix = settingsBuilder.getCollectionPrefix();
        if (collectionPrefix != null) {
            collectionBaseName = collectionPrefix + collectionBaseName;
        }

        String collectionSuffix = settingsBuilder.getCollectionSuffix();
        if (collectionSuffix != null) {
            collectionBaseName = collectionBaseName + collectionSuffix;
        }

        if (!COLLECTION_REGEX_NAME.matcher(collectionBaseName).matches()) {
            throw new RepositoryNameInvalidException(repositoryClass,
                COLLECTION_REGEX_NAME.pattern(), collectionBaseName);
        }

        return collectionBaseName;
    }

    public Class<?> parseValidatableReturnType(Method method) {
        ParameterizedType parameterizedType = decapsulateFuture(method);
        Class<?> entityTypeClass;
        if (parameterizedType == null) {
            entityTypeClass = method.getReturnType();
        } else {
            entityTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return PrimitiveUtils.wrapperOf(entityTypeClass);
    }

    public ParameterizedType decapsulateFuture(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!CompletableFuture.class.isAssignableFrom(returnType)) {
            if (java.util.Collection.class.isAssignableFrom(returnType)) {
                return (ParameterizedType) method.getGenericReturnType();
            }
            return null;
        }
        ParameterizedType futureGenericType = (ParameterizedType) method.getGenericReturnType();
        Type innerFutureType = futureGenericType.getActualTypeArguments()[0];
        if (!(innerFutureType instanceof ParameterizedType)) {
            return futureGenericType;
        }
        return (ParameterizedType) innerFutureType;
    }

    public String parseBsonName(Field field) {
        TransformField transformField = field.getAnnotation(TransformField.class);
        if (transformField != null && !transformField.value().trim().equalsIgnoreCase("")) {
            return transformField.value();
        }
        return field.getName();
    }

    /**
     * This method is ued to get all annotations from an entity class, using while loop,
     * to iterate through all super-types.
     *
     * @param entityClass     The class, which should be scanned.
     * @param annotationClass the searched annotation
     * @param <E>             The generic type of the Class
     * @param <A>             The generic type of the annotation Class
     * @return The Set with all found annotations of type A
     */
    public <E, A extends Annotation> Set<A> getAllAnnotations(Class<E> entityClass,
                                                               Class<A> annotationClass) {
        Set<A> annotationSet = new HashSet<>();
        Class<?> clazz = entityClass;
        while (clazz != Object.class) {
            A[] indexArray = clazz.getAnnotationsByType(annotationClass);
            clazz = clazz.getSuperclass();
            if (indexArray.length == 0) {
                continue;
            }
            annotationSet.addAll(Arrays.asList(indexArray));
        }
        return annotationSet;
    }
}
