package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodGetUniqueId extends GlobalPredefinedMethod {

    public MethodGetUniqueId() {
        super("getUniqueId");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        E entity = getGenericEntity(repositoryData, method, arguments[0]);
        return getGenericUniqueIdByEntity(repositoryData, method, entity);
    }
}
