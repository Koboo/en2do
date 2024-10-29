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
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        MongoCollection<E> entityCollection = repositoryData.getEntityCollection();
        List<E> insertList = checkEntityList(repositoryData, method, arguments[0]);
        if (insertList.isEmpty()) {
            return true;
        }
        // Using "insertMany" should speed up inserting performance drastically
        InsertManyResult result = entityCollection.insertMany(insertList);
        return result.wasAcknowledged();
    }
}
