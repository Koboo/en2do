package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodDelete extends GlobalPredefinedMethod {

    public MethodDelete() {
        super("delete");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        MongoCollection<E> collection = repositoryData.getEntityCollection();
        E entity = checkEntity(repositoryData, method, arguments[0]);
        ID uniqueId = checkUniqueIdByEntity(repositoryData, method, entity);
        Bson idFilter = createIdFilter(uniqueId);
        DeleteResult result = collection.deleteOne(idFilter);
        return result.wasAcknowledged();
    }
}
