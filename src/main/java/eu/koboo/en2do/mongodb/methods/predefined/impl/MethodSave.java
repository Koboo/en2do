package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MethodSave extends GlobalPredefinedMethod {

    ReplaceOptions replaceOptions;

    public MethodSave() {
        super("save");
        this.replaceOptions = new ReplaceOptions().upsert(true);
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        E entity = checkEntity(repositoryData, method, arguments[0]);
        ID uniqueId = checkUniqueIdByEntity(repositoryData, method, entity);
        Bson idFilter = createIdFilter(uniqueId);
        MongoCollection<E> entityCollection = repositoryData.getEntityCollection();
        if (entityCollection.countDocuments(idFilter) > 0) {
            UpdateResult result = entityCollection.replaceOne(idFilter, entity, replaceOptions);
            return result.wasAcknowledged();
        }
        entityCollection.insertOne(entity);
        return true;
    }
}
