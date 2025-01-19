package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertManyResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MethodInsertAll extends GlobalPredefinedMethod {

    public MethodInsertAll() {
        super("insertAll");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        MongoCollection<E> entityCollection = repositoryData.getEntityCollection();

        Class<E> entityClass = repositoryData.getEntityClass();
        List<E> insertList = (List<E>) arguments[0];
        if (insertList == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " as parameter of method " +
                method.getName() + " is null.");
        }
        if (insertList.isEmpty()) {
            return true;
        }
        // Using "insertMany" should speed up inserting performance drastically
        InsertManyResult result = entityCollection.insertMany(insertList);
        return result.wasAcknowledged();
    }
}
