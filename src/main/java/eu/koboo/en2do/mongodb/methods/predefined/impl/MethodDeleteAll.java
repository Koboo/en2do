package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodDeleteAll extends GlobalPredefinedMethod {

    public MethodDeleteAll() {
        super("deleteAll");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        Bson idExistsFilter = createIdExistsFilter();
        DeleteResult deleteResult = repositoryData.getEntityCollection().deleteMany(idExistsFilter);
        return deleteResult.wasAcknowledged();
    }
}
