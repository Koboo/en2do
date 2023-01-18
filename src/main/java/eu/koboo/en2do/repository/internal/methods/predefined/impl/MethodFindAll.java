package eu.koboo.en2do.repository.internal.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.RepositoryMeta;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodFindAll<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodFindAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("findAll", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        FindIterable<E> findIterable = repositoryMeta.createIterable(null, methodName);
        return findIterable.into(new ArrayList<>());
    }
}
