package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodDelete<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodDelete(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("delete", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        DeleteResult result = entityCollection.deleteOne(idFilter);
        return result.wasAcknowledged();
    }
}
