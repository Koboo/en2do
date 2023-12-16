package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class MethodDeleteMany<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodDeleteMany(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("deleteMany", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        List<E> entityList = repositoryMeta.checkEntityList(method, arguments[0]);
        if(entityList.isEmpty()) {
            return true;
        }
        List<ID> idList = new LinkedList<>();
        for (E entity : entityList) {
            ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
            idList.add(uniqueId);
        }
        Bson idFilter = Filters.in("_id", idList);
        entityCollection.deleteMany(idFilter);
        return true;
    }
}
