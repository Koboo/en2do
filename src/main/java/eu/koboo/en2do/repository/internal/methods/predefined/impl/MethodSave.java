package eu.koboo.en2do.repository.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.RepositoryMeta;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MethodSave<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    ReplaceOptions replaceOptions;

    public MethodSave(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("save", meta, entityCollection);
        this.replaceOptions = new ReplaceOptions().upsert(true);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        E entity = repositoryMeta.checkEntity(method, arguments[0]);
        ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        if (entityCollection.countDocuments(idFilter) > 0) {
            UpdateResult result = entityCollection.replaceOne(idFilter, entity, replaceOptions);
            return result.wasAcknowledged();
        }
        entityCollection.insertOne(entity);
        return true;
    }
}
