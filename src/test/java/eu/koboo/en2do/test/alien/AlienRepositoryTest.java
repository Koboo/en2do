package eu.koboo.en2do.test.alien;

import eu.koboo.en2do.test.RepositoryTest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AlienRepositoryTest extends RepositoryTest<Alien, UUID, AlienRepository> {

    @Override
    public @NotNull Class<AlienRepository> repositoryClass() {
        return AlienRepository.class;
    }
}
