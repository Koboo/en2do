package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;

import java.lang.reflect.Method;

public class MethodGetEntityClass<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodGetEntityClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getEntityClass", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        return repositoryMeta.getEntityClass();
    }
}
