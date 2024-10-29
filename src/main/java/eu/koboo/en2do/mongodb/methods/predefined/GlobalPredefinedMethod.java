package eu.koboo.en2do.mongodb.methods.predefined;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

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
     * @param <E>            The generic type of the entity
     * @param <ID>           The generic type of the entity id
     * @param <R>            The generic type of the repository
     * @param repositoryData The repository meta of the called repository
     * @param method         The method, which should be invoked
     * @param arguments      The object array, which represents the arguments of the method
     * @return The object created by the method invocation
     * @throws Exception any, if something bad happens
     */
    public abstract <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                                       Method method,
                                                                       Object[] arguments) throws Exception;

    protected <ID> Bson createIdFilter(ID uniqueId) {
        return Filters.eq("_id", uniqueId);
    }

    protected Bson createIdExistsFilter() {
        return Filters.exists("_id");
    }

    protected <E, ID, R extends Repository<E, ID>> ID checkUniqueIdByEntity(RepositoryData<E, ID, R> repositoryData,
                                                                            Method method, E entity) throws IllegalAccessException {
        Class<ID> entityIdClass = repositoryData.getEntityUniqueIdClass();
        Field entityIdField = repositoryData.getEntityUniqueIdField();
        ID uniqueId = entityIdClass.cast(entityIdField.get(entity));
        if (uniqueId == null) {
            Class<E> entityClass = repositoryData.getEntityClass();
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        return uniqueId;
    }

    protected <E, ID, R extends Repository<E, ID>> ID checkUniqueIdByArgument(RepositoryData<E, ID, R> repositoryData,
                                                                              Method method, Object argument) {
        Class<ID> entityIdClass = repositoryData.getEntityUniqueIdClass();
        ID uniqueId = entityIdClass.cast(argument);
        if (uniqueId == null) {
            Class<E> entityClass = repositoryData.getEntityClass();
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        return uniqueId;
    }

    protected <E, ID, R extends Repository<E, ID>> E checkEntity(RepositoryData<E, ID, R> repositoryData,
                                                                 Method method, Object argument) {
        Class<E> entityClass = repositoryData.getEntityClass();
        E entity = entityClass.cast(argument);
        if (entity == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    protected <E, ID, R extends Repository<E, ID>> Collection<E> checkEntityCollection(RepositoryData<E, ID, R> repositoryData,
                                                                                       Method method, Object argument) {
        Class<E> entityClass = repositoryData.getEntityClass();
        Collection<E> entity = (Collection<E>) argument;
        if (entity == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    protected <E, ID, R extends Repository<E, ID>> List<E> checkEntityList(RepositoryData<E, ID, R> repositoryData,
                                                                           Method method, Object argument) {
        Class<E> entityClass = repositoryData.getEntityClass();
        List<E> entity = (List<E>) argument;
        if (entity == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " as parameter of method " +
                method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    protected <E, ID, R extends Repository<E, ID>> Collection<ID> checkUniqueIdList(RepositoryData<E, ID, R> repositoryData,
                                                                                    Method method, Object argument) {
        Class<E> entityClass = repositoryData.getEntityClass();
        Collection<ID> entity = (Collection<ID>) argument;
        if (entity == null) {
            throw new NullPointerException("ID-List of Entities of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        return entity;
    }

}
