package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;

import java.lang.reflect.Method;

public class MethodGetUniqueIdClass<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodGetUniqueIdClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super(meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        Object identifier = repositoryMeta.getEntityUniqueIdField().get(entity);
        return repositoryMeta.checkUniqueId(method, identifier);
    }
}
