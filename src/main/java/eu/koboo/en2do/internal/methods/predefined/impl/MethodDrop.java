package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodDrop<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodDrop(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("drop", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        entityCollection.drop();
        return true;
    }
}
