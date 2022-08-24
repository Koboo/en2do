package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Repository;
import eu.koboo.en2do.MongoManager;

import java.util.UUID;
import java.util.concurrent.Executors;

public class CustomerRepository extends Repository<Customer, UUID> {

    public CustomerRepository(MongoManager mongoManager) {
        super(mongoManager, Executors.newSingleThreadExecutor());
    }
}