package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodSetUniqueId extends GlobalPredefinedMethod {

    public MethodSetUniqueId() {
        super("setUniqueId");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        E entity = getGenericEntity(repositoryData, method, arguments[0]);
        ID entityId = getGenericUniqueIdByArgument(repositoryData, method, arguments[1]);
        Field entityUniqueIdField = repositoryData.getEntityUniqueIdField();
        try {
            entityUniqueIdField.setAccessible(true);
            entityUniqueIdField.set(entity, entityId);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't set entity unique id of type " +
                repositoryData.getEntityClass().getName() + " in method " + method.getName() + ".", e);
        }
        return null;
    }
}
