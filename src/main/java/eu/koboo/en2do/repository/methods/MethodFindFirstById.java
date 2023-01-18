package eu.koboo.en2do.repository.methods;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.RepositoryMeta;
import eu.koboo.en2do.repository.RepositoryMethod;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;

public class MethodFindFirstById<E, ID, R extends Repository<E, ID>> extends RepositoryMethod<E, ID, R> {

    public MethodFindFirstById(RepositoryMeta<E, ID, R> meta, MongoCollection<E> entityCollection) {
        super("findFirstById", meta, entityCollection);
    }

    @Override
    public Object handle(Method method, Object[] arguments) throws Exception {
        ID uniqueId = repositoryMeta.checkUniqueId(method, arguments[0]);
        Bson idFilter = repositoryMeta.createIdFilter(uniqueId);
        FindIterable<E> findIterable = entityCollection.find(idFilter);
        if (repositoryMeta.isAppendMethodAsComment()) {
            findIterable.comment("en2do \"" + getMethodName() + "\"");
        }
        return findIterable.limit(1).first();
    }
}
