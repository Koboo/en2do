package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryMeta;
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
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        Collection<ID> idList = repositoryMeta.checkUniqueIdList(method, arguments[0]);
        if (idList.isEmpty()) {
            return true;
        }
        Bson idFilter = Filters.in("_id", idList);
        DeleteResult deleteResult = repositoryMeta.getEntityCollection().deleteMany(idFilter);
        return deleteResult.wasAcknowledged();
    }
}
