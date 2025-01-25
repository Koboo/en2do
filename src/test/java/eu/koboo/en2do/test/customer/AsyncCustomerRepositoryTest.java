package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class AsyncCustomerRepositoryTest extends RepositoryTest<Customer, UUID, AsyncCustomerRepository> {

    @Override
    public Class<AsyncCustomerRepository> repositoryClass() {
        return AsyncCustomerRepository.class;
    }
}
