package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodFindManyByFilter extends GlobalPredefinedMethod {

    public MethodFindManyByFilter() {
        super("findManyByFilter");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        Bson filter = (Bson) arguments[0];
        FindIterable<E> findIterable = repositoryMeta.createIterable(filter, methodName);
        return findIterable.into(new ArrayList<>());
    }
}
