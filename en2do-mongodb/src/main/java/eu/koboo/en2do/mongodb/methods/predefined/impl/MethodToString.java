package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodToString<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodToString(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("toString", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        Class<R> repositoryClass = repositoryMeta.getRepositoryClass();
        return repositoryClass.getName();
    }
}
