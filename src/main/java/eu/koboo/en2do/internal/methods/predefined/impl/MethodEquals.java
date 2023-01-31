package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodEquals<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodEquals(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("equals", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        Object object = arguments[0];
        if (object == null) {
            return false;
        }
        if (!(object instanceof Repository<?, ?>)) {
            return false;
        }
        Repository<?, ?> repository = (Repository<?, ?>) object;
        return repository.getClass().getName().equalsIgnoreCase(repositoryMeta.getClass().getName());
    }
}
