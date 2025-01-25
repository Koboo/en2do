package eu.koboo.en2do.test.user;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@Collection("user_repository")
public interface UserRepository extends Repository<User, UUID> {

    User findFirstByUniqueId(UUID uniqueId);

    List<User> findAllByUniqueId(UUID uniqueId);

    CompletableFuture<User> findOneByUniqueId(UUID uniqueId);

    CompletableFuture<List<User>> findManyByUniqueId(UUID uniqueId);
}
