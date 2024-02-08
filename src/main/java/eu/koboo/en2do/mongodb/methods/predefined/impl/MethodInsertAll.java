package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertManyResult;
import eu.koboo.en2do.mongodb.RepositoryMeta;
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
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        MongoCollection<E> entityCollection = repositoryMeta.getEntityCollection();
        List<E> insertList = repositoryMeta.checkEntityList(method, arguments[0]);
        if (insertList.isEmpty()) {
            return true;
        }
        // Using "insertMany" should speed up inserting performance drastically
        InsertManyResult result = entityCollection.insertMany(insertList);
        return result.wasAcknowledged();
    }
}
