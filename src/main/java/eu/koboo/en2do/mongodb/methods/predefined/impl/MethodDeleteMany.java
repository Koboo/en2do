package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MethodDeleteMany extends GlobalPredefinedMethod {

    public MethodDeleteMany() {
        super("deleteMany");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        Collection<E> entityList = checkEntityCollection(repositoryData, method, arguments[0]);
        if (entityList.isEmpty()) {
            return true;
        }
        List<ID> idList = new LinkedList<>();
        for (E entity : entityList) {
            ID uniqueId = checkUniqueIdByEntity(repositoryData, method, entity);
            idList.add(uniqueId);
        }
        Bson idFilter = Filters.in("_id", idList);
        DeleteResult deleteResult = repositoryData.getEntityCollection().deleteMany(idFilter);
        return deleteResult.wasAcknowledged();
    }
}
