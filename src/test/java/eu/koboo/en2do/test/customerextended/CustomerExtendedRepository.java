package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.repository.*;
import eu.koboo.en2do.repository.methods.async.Async;

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
