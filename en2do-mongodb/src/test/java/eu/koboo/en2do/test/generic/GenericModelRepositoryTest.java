package eu.koboo.en2do.test.generic;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class GenericModelRepositoryTest extends RepositoryTest<GenericModelImpl, UUID, GenericModelRepository> {

    @Override
    public Class<GenericModelRepository> repositoryClass() {
        return GenericModelRepository.class;
    }
}
