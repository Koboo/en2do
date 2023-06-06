package eu.koboo.en2do.test.generic;

import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.test.user.User;

import java.util.UUID;

@SuppressWarnings("unused")
@Collection("generic_repository")
public interface GenericModelRepository extends Repository<GenericModelImpl, UUID> {

}