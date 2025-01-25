package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@Collection("customer_repository")
public interface AsyncCustomerRepository extends Repository<Customer, UUID> {

    CompletableFuture<Customer> findFirstByUniqueId(UUID id);

    CompletableFuture<List<Customer>> findAllByUniqueIdExists();
}
