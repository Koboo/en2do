package eu.koboo.en2do.test.generic;

import eu.koboo.en2do.test.RepositoryTest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GenericModelRepositoryTest extends RepositoryTest<GenericModelImpl, UUID, GenericModelRepository> {

    @Override
    public @NotNull Class<GenericModelRepository> repositoryClass() {
        return GenericModelRepository.class;
    }
}
