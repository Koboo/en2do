package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.List;

public class MethodDeleteAll<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodDeleteAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super(meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        List<E> entityList = repositoryMeta.checkEntityList(method, arguments[0]);
        for (E entity : entityList) {
            ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
            Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
            entityCollection.deleteOne(idFilter);
        }
        return true;
    }
}
