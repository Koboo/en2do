package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodDeleteById extends GlobalPredefinedMethod {

    public MethodDeleteById() {
        super("deleteById");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        MongoCollection<E> collection = repositoryData.getEntityCollection();
        ID uniqueId = checkUniqueIdByArgument(repositoryData, method, arguments[0]);
        Bson idFilter = createIdFilter(uniqueId);
        DeleteResult result = collection.deleteOne(idFilter);
        return result.wasAcknowledged();
    }
}
