package eu.koboo.en2do.repository.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.RepositoryMeta;
import eu.koboo.en2do.repository.methods.predefined.PredefinedRepositoryMethod;

import java.lang.reflect.Method;

public class MethodGetEntityUniqueIdClass<E, ID, R extends Repository<E, ID>> extends PredefinedRepositoryMethod<E, ID, R> {

    public MethodGetEntityUniqueIdClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getEntityUniqueIdClass", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        return repositoryMeta.getEntityUniqueIdClass();
    }
}
