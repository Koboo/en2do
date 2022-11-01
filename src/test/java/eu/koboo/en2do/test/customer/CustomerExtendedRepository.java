package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.annotation.Collection;
import eu.koboo.en2do.sort.Sort;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Collection("customer_extended_repository")
public interface CustomerExtendedRepository extends Repository<CustomerExtended, UUID> {

    CustomerExtended findByFirstName(String firstName);
}
