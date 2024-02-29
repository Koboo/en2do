package eu.koboo.en2do.mongodb.methods.predefined.impl;

import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.repository.Repository;

import java.lang.reflect.Method;

public class MethodDeleteAll extends GlobalPredefinedMethod {

    public MethodDeleteAll() {
        super("deleteAll");
    }

    @Override
    public <E, ID, R extends Repository<E, ID>> Object handle(RepositoryMeta<E, ID, R> repositoryMeta,
                                                              Method method, Object[] arguments) throws Exception {
        DeleteResult deleteResult = repositoryMeta.getEntityCollection().deleteMany(repositoryMeta.createIdExistsFilter());
        return deleteResult.wasAcknowledged();
    }
}
