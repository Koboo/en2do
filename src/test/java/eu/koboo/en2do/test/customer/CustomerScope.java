package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Scope;

import java.util.UUID;

public class CustomerScope extends Scope<Customer, UUID> {

    public CustomerScope(CustomerRepository repository) {
        super(repository);
    }
}