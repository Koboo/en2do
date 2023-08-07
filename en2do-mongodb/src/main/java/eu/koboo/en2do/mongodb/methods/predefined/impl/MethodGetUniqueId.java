package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodGetUniqueId<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodGetUniqueId(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getUniqueId", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        Object identifier = repositoryMeta.getEntityUniqueIdField().get(entity);
        return repositoryMeta.checkUniqueId(method, identifier);
    }
}
