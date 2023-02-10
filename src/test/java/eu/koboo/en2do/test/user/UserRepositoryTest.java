package eu.koboo.en2do.test.user;

import eu.koboo.en2do.test.RepositoryTest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserRepositoryTest extends RepositoryTest<User, UUID, UserRepository> {

    @Override
    public @NotNull Class<UserRepository> repositoryClass() {
        return UserRepository.class;
    }
}
