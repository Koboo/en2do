package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.FilterScope;

import java.util.UUID;

public class CustomerScope extends FilterScope<Customer, UUID> {

    public CustomerScope(CustomerRepository repository) {
        super(repository);
    }
}