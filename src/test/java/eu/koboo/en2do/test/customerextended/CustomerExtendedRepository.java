package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.DropEntitiesOnStart;
import eu.koboo.en2do.repository.DropIndexesOnStart;
import eu.koboo.en2do.repository.Repository;

import java.util.UUID;

@Collection("customer_extended_repository")
@DropIndexesOnStart
@DropEntitiesOnStart
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findFirstByFirstName(String firstName);
}
