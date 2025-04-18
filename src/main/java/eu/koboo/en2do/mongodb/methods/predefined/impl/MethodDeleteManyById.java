package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.Collection;

public class MethodDeleteManyById extends GlobalPredefinedMethod {

    public MethodDeleteManyById() {
        super("deleteManyById");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        Class<E> entityClass = repositoryData.getEntityClass();
        Collection<ID> idList = (Collection<ID>) arguments[0];
        if (idList == null) {
            throw new NullPointerException("idList of Entities of type " + entityClass.getName() +
                " as parameter of method " + method.getName() + " is null.");
        }
        if (idList.isEmpty()) {
            return true;
        }
        Bson idFilter = Filters.in("_id", idList);
        DeleteResult deleteResult = repositoryData.getEntityCollection().deleteMany(idFilter);
        return deleteResult.wasAcknowledged();
    }
}
