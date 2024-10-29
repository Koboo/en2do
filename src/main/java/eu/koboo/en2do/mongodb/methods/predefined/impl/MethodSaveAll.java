package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MethodSaveAll extends GlobalPredefinedMethod {

    ReplaceOptions replaceOptions;

    public MethodSaveAll() {
        super("saveAll");
        this.replaceOptions = new ReplaceOptions().upsert(true);
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        MongoCollection<E> entityCollection = repositoryData.getEntityCollection();
        Collection<E> entityList = checkEntityCollection(repositoryData, method, arguments[0]);
        if (entityList.isEmpty()) {
            return true;
        }
        List<E> insertList = new ArrayList<>();
        // Iterate through entities and check if it already exists by unique identifier.
        for (E entity : entityList) {
            ID uniqueId = checkUniqueIdByEntity(repositoryData, method, entity);
            Bson idFilter = createIdFilter(uniqueId);
            if (entityCollection.countDocuments(idFilter) > 0) {
                // Entity exists, so we want to update the existing document.
                entityCollection.replaceOne(idFilter, entity, replaceOptions);
                continue;
            }
            // Entity doesn't exist, so we want to insert a new document.
            insertList.add(entity);
        }
        // Using "insertMany" should speed up inserting performance drastically
        if (!insertList.isEmpty()) {
            entityCollection.insertMany(insertList);
        }
        return true;
    }
}
