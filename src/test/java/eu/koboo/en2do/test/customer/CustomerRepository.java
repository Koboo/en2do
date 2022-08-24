package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.AbstractRepository;
import eu.koboo.en2do.MongoManager;

import java.util.UUID;
import java.util.concurrent.Executors;

public class CustomerRepository extends AbstractRepository<Customer, UUID> {

    public CustomerRepository(MongoManager mongoManager) {
        super(mongoManager, Executors.newSingleThreadExecutor());
    }
}