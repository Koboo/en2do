package eu.koboo.en2do.test.user;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.UUID;

@SuppressWarnings("unused")
@Collection("user_repository")
public interface UserRepository extends Repository<User, UUID> {

}
