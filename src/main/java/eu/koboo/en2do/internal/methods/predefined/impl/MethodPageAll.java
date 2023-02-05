package eu.koboo.en2do.internal.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodPageAll<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodPageAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("pageAll", meta, entityCollection);
    }

    @Override
    public @Nullable Object handle(@NotNull Method method, @NotNull Object[] arguments) throws Exception {
        FindIterable<E> findIterable = repositoryMeta.createIterable(null, methodName);
        findIterable = repositoryMeta.applyPageObject(method, findIterable, arguments);
        return findIterable.into(new ArrayList<>());
    }
}
