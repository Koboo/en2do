package eu.koboo.en2do.test.alien;

import eu.koboo.en2do.Collection;
import eu.koboo.en2do.Repository;

import java.util.UUID;

@Collection("alien_repository")
public interface AlienRepository extends Repository<Alien, UUID> {

}
