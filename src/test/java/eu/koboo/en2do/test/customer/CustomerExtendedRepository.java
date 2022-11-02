package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Collection;
import eu.koboo.en2do.Repository;

import java.util.UUID;

@Collection("customer_extended_repository")
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findByFirstName(String firstName);
}
