package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryMeta;
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
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        Collection<E> entityList = repositoryMeta.checkEntityCollection(method, arguments[0]);
        if (entityList.isEmpty()) {
            return true;
        }
        List<ID> idList = new LinkedList<>();
        for (E entity : entityList) {
            ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
            idList.add(uniqueId);
        }
        Bson idFilter = Filters.in("_id", idList);
        DeleteResult deleteResult = repositoryMeta.getEntityCollection().deleteMany(idFilter);
        return deleteResult.wasAcknowledged();
    }
}
