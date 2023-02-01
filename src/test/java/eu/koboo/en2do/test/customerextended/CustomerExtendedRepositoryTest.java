package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.test.RepositoryTest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CustomerExtendedRepositoryTest extends RepositoryTest<CustomerExtended, UUID, CustomerExtendedRepository> {

    @Override
    public @NotNull Class<CustomerExtendedRepository> repositoryClass() {
        return CustomerExtendedRepository.class;
    }
}
