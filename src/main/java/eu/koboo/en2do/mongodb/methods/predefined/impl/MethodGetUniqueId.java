package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodGetUniqueId extends GlobalPredefinedMethod {

    public MethodGetUniqueId() {
        super("getUniqueId");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        Object identifier = repositoryMeta.getEntityUniqueIdField().get(entity);
        return repositoryMeta.checkUniqueId(method, identifier);
    }
}
