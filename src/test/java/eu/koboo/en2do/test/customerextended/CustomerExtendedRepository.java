package eu.koboo.en2do.test.customerextended;

import eu.koboo.en2do.Collection;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.meta.options.DropEntitiesOnStart;
import eu.koboo.en2do.meta.options.DropIndexesOnStart;

import java.util.UUID;

@Collection("customer_extended_repository")
@DropIndexesOnStart
@DropEntitiesOnStart
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findFirstByFirstName(String firstName);
}
