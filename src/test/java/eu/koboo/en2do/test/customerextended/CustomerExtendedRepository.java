package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.repository.options.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;

import java.util.UUID;

@Collection("customer_extended_repository")
@DropIndexesOnStart
@DropEntitiesOnStart
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findFirstByFirstName(String firstName);
}
