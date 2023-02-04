package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MethodGetEntityUniqueIdClass<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodGetEntityUniqueIdClass(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("getEntityUniqueIdClass", meta, entityCollection);
    }

    @Override
    public @Nullable Object handle(@NotNull Method method, @NotNull Object[] arguments) throws Exception {
        return repositoryMeta.getEntityUniqueIdClass();
    }
}
