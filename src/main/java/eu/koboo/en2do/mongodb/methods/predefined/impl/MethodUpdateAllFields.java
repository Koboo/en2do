package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;

import java.lang.reflect.Method;

public class MethodUpdateAllFields extends GlobalPredefinedMethod {

    public MethodUpdateAllFields() {
        super("updateAllFields");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        // Cast the first object of the array to the UpdateBatch object
        MongoCollection<E> collection = repositoryMeta.getEntityCollection();
        UpdateBatch updateBatch = (UpdateBatch) arguments[0];

        // Call the UpdateBatch on all documents with the "id" field of the entity,
        // which could be a unique name or the "_id" field.
        UpdateResult result = collection.updateMany(repositoryMeta.createIdExistsFilter(),
            repositoryMeta.createUpdateDocument(updateBatch),
            new UpdateOptions().upsert(false));
        return result.wasAcknowledged();
    }
}