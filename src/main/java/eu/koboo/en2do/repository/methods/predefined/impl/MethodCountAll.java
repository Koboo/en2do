package eu.koboo.en2do.repository.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.RepositoryMeta;
import eu.koboo.en2do.repository.methods.predefined.PredefinedRepositoryMethod;

import java.lang.reflect.Method;

public class MethodCountAll<E, ID, R extends Repository<E, ID>> extends PredefinedRepositoryMethod<E, ID, R> {

    public MethodCountAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("countAll", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        return entityCollection.countDocuments();
    }
}
