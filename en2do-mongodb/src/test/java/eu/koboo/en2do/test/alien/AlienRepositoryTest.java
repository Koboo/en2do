package eu.koboo.en2do.test.alien;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class AlienRepositoryTest extends RepositoryTest<Alien, UUID, AlienRepository> {

    @Override
    public Class<AlienRepository> repositoryClass() {
        return AlienRepository.class;
    }
}
