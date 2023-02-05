package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MethodFindFirstById<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodFindFirstById(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("findFirstById", meta, entityCollection);
    }

    @Override
    public @Nullable Object handle(@NotNull Method method, @NotNull Object[] arguments) throws Exception {
        ID uniqueId = repositoryMeta.checkUniqueId(method, arguments[0]);
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        FindIterable<E> findIterable = repositoryMeta.createIterable(idFilter, methodName);
        return findIterable.limit(1).first();
    }
}
