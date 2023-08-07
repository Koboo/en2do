package eu.koboo.en2do.test.user;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class UserRepositoryTest extends RepositoryTest<User, UUID, UserRepository> {

    @Override
    public Class<UserRepository> repositoryClass() {
        return UserRepository.class;
    }
}
