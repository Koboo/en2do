package eu.koboo.en2do.test.mapkeys;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.UUID;

@Collection("mapkey")
public interface MapKeyRepository extends Repository<MapKey, UUID> {

}
