package eu.koboo.en2do.repository.methods.predefined;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.RepositoryMeta;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class PredefinedRepositoryMethod<E, ID, R extends Repository<E, ID>> {

    @Getter
    String methodName;
    RepositoryMeta<E, ID, R> repositoryMeta;
    MongoCollection<E> entityCollection;

    public abstract Object handle(Method method, Object[] arguments) throws Exception;
}
