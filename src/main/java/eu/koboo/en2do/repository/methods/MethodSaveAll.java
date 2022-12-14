package eu.koboo.en2do.repository.methods;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodSaveAll<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodSaveAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("saveAll", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        List<E> entityList = repositoryMeta.checkEntityList(method, arguments[0]);
        if (entityList.isEmpty()) {
            return true;
        }
        List<E> insertList = new ArrayList<>();
        // Iterate through entities and check if it already exists by uniqueidentifier.
        ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
        for (E entity : entityList) {
            ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
            Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
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
