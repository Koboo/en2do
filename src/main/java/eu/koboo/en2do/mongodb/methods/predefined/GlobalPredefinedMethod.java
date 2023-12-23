package eu.koboo.en2do.mongodb.methods.predefined;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

/**
 * This class is a representation of a predefined method from the repository
 */
@Getter
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class GlobalPredefinedMethod {

    String methodName;

    /**
     * Invokes the method and returns the created object.
     *
     * @param method    The method, which should be invoked
     * @param arguments The object array, which represents the arguments of the method
     * @return The object created by the method invocation
     * @throws Exception any, if something bad happens
     */
    public abstract <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                                       Method method,
                                                                       Object[] arguments) throws Exception;
}
