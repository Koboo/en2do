package dev.binflux.en2do.test.impl;

import eu.koboo.en2do.Scope;
import eu.koboo.en2do.test.customer.Customer;

import java.util.UUID;

public class CustomerScope extends Scope<Customer, UUID> {

    public CustomerScope(CustomerRepository repository) {
        super(repository);
    }
}