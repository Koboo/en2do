package eu.koboo.en2do.repository.internal.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.RepositoryMeta;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodSortAll<E, ID, R extends Repository<E, ID>> extends PredefinedMethod<E, ID, R> {

    public MethodSortAll(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("sortAll", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        FindIterable<E> findIterable = entityCollection.find();
        findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
        if (repositoryMeta.isAppendMethodAsComment()) {
            findIterable.comment("en2do \"" + getMethodName() + "\"");
        }
        return findIterable.into(new ArrayList<>());
    }
}
