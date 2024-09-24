package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodExistsById extends GlobalPredefinedMethod {

    public MethodExistsById() {
        super("existsById");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        ID uniqueId = repositoryData.checkUniqueId(method, arguments[0]);
        Bson idFilter = repositoryData.createIdFilter(uniqueId);
        return repositoryData.getEntityCollection().countDocuments(idFilter) > 0;
    }
}
