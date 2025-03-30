package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.FindIterable;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MethodFilterBy extends GlobalPredefinedMethod {

    public MethodFilterBy() {
        super("filterBy");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        Object filterArg = arguments[0];
        if(!(filterArg instanceof Bson)) {
            throw new IllegalArgumentException();
        }
        Bson bsonFilter = (Bson) filterArg;
        FindIterable<E> findIterable = repositoryData.createFindIterableBase(bsonFilter, methodName);
        return findIterable.into(new ArrayList<>());
    }
}
