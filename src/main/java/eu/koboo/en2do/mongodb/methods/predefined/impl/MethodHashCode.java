package eu.koboo.en2do.mongodb.methods.predefined.impl;

import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodHashCode extends GlobalPredefinedMethod {

    public MethodHashCode() {
        super("hashCode");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        Class<R> repositoryClass = repositoryMeta.getRepositoryClass();
        return repositoryClass.getName().hashCode() + repositoryMeta.getCollectionName().hashCode();
    }
}
