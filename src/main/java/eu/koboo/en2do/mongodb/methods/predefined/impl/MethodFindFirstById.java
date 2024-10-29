package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodFindFirstById extends GlobalPredefinedMethod {

    public MethodFindFirstById() {
        super("findFirstById");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        ID uniqueId = checkUniqueIdByArgument(repositoryData, method, arguments[0]);
        Bson idFilter = createIdFilter(uniqueId);
        FindIterable<E> findIterable = repositoryData.createIterable(idFilter, methodName);
        return findIterable.limit(1).first();
    }
}
