package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;

import java.lang.reflect.Method;

public class MethodGetClass<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodGetClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getClass", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        return repositoryMeta.getRepositoryClass();
    }
}
