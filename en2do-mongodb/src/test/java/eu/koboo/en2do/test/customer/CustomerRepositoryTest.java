package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.test.RepositoryTest;

import java.util.UUID;

public class CustomerRepositoryTest extends RepositoryTest<Customer, UUID, CustomerRepository> {

    @Override
    public Class<CustomerRepository> repositoryClass() {
        return CustomerRepository.class;
    }
}
