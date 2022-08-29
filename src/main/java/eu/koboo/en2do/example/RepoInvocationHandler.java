package eu.koboo.en2do.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RepoInvocationHandler<E, ID> implements InvocationHandler {

    private final MongoCollection<E> collection;

    public RepoInvocationHandler(MongoCollection<E> collection) {
        this.collection = collection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if(methodName.equalsIgnoreCase("save")) {
            E entity = (E) args[0];
            ID uniqueId = getIdFromEntity(entity);
            Bson idFilter = Filters.eq(entityIdField.getName(), uniqueId);
            if (document.isEmpty()) {
                return delete(idFilter);
            }
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult result = collection.updateOne(idFilter, entity, options);
            return result.wasAcknowledged();
        }
        throw new RuntimeException("No method-handler found.");
    }
}