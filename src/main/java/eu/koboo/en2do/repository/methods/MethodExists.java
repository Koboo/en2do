package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodExists<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodExists(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("exists", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        return entityCollection.countDocuments(idFilter) > 0;
    }
}
