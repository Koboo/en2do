package eu.koboo.en2do.repository.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.RepositoryMeta;
import eu.koboo.en2do.repository.methods.predefined.PredefinedRepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodExistsById<E, ID, R extends Repository<E, ID>> extends PredefinedRepositoryMethod<E, ID, R> {

    public MethodExistsById(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("existsById", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        ID uniqueId = repositoryMeta.checkUniqueId(method, arguments[0]);
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        return entityCollection.countDocuments(idFilter) > 0;
    }
}
