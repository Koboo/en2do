package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public final class MethodEquals extends GlobalPredefinedMethod {

    public MethodEquals() {
        super("equals");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryData<E, ID, R> repositoryData,
                                                              Method method, Object[] arguments) {
        if (arguments.length != 1) {
            return false;
        }
        Object object = arguments[0];
        if (!(object instanceof Repository<?, ?> repository)) {
            return false;
        }
        return repository.getClass().getName().equalsIgnoreCase(repositoryData.getClass().getName());
    }
}
