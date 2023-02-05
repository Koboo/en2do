package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MethodGetCollectionName<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodGetCollectionName(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getCollectionName", meta, entityCollection);
    }

    @Override
    public @Nullable Object handle(@NotNull Method method, @NotNull Object[] arguments) throws Exception {
        return repositoryMeta.getCollectionName();
    }
}
