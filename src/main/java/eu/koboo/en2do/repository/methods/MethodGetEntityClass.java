package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;

import java.lang.reflect.Method;

public class MethodGetEntityClass<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodGetEntityClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super(meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        return repositoryMeta.getEntityClass();
    }
}
