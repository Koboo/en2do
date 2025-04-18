package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodPageAll extends GlobalPredefinedMethod {

    public MethodPageAll() {
        super("pageAll");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) throws Exception {
        FindIterable<E> findIterable = repositoryData.createFindIterableBase(null, methodName);
        findIterable = repositoryData.applyPageObject(method, findIterable, arguments);
        return findIterable.into(new ArrayList<>());
    }
}
