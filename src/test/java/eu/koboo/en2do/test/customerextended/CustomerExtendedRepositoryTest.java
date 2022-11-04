package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class CustomerExtendedRepositoryTest extends RepositoryTest<CustomerExtended, UUID, CustomerExtendedRepository> {

    @Override
    public Class<CustomerExtendedRepository> repositoryClass() {
        return CustomerExtendedRepository.class;
    }
}
