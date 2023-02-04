package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class MethodDeleteAll<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodDeleteAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("deleteAll", meta, entityCollection);
    }

    @Override
    public @Nullable Object handle(@NotNull Method method, @NotNull Object[] arguments) throws Exception {
        List<E> entityList = repositoryMeta.checkEntityList(method, arguments[0]);
        for (E entity : entityList) {
            ID uniqueId = repositoryMeta.checkUniqueId(method, repositoryMeta.getUniqueId(entity));
            Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
            entityCollection.deleteOne(idFilter);
        }
        return true;
    }
}
