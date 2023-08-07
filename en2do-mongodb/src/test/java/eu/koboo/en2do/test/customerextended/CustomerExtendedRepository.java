package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Collection("customer_extended_repository")
@DropIndexesOnStart
@DropEntitiesOnStart
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID>, AsyncRepository<CustomerExtended, UUID> {

    CustomerExtended findFirstByFirstName(String firstName);

    @Async
    CompletableFuture<CustomerExtended> findFirstByFirstNameExists();
}
