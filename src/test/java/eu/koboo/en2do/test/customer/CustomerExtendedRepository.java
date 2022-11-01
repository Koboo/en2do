package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Repository;
import eu.koboo.en2do.Collection;

import java.util.UUID;

@Collection("customer_extended_repository")
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findByFirstName(String firstName);
}
