package eu.koboo.en2do.repository;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.meta.registry.MethodHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class RepositoryMethod<E, ID, R extends Repository<E, ID>> implements MethodHandler {

    @Getter
    String methodName;
    RepositoryMeta<E, ID, R> repositoryMeta;
    MongoCollection<E> entityCollection;
}
