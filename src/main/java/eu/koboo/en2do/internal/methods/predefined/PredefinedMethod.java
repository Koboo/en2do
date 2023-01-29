package eu.koboo.en2do.internal.methods.predefined;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

/**
 * This class is a representation of a predefined method from the repository
 * @param <E> The generic type of the entity
 * @param <ID> The generic type of the id of the entity
 * @param <R> The generic type of the repository
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class PredefinedMethod<E, ID, R extends Repository<E, ID>> {

    @Getter
    String methodName;
    RepositoryMeta<E, ID, R> repositoryMeta;
    MongoCollection<E> entityCollection;

    /**
     * Invokes the method and returns the created object.
     * @param method The method, which should be invoked
     * @param arguments The object array, which represents the arguments of the method
     * @return The object created by the method invocation
     * @throws Exception any, if something bad happens
     */
    public abstract Object handle(Method method, Object[] arguments) throws Exception;
}
