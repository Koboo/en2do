package eu.koboo.en2do.test.keytest;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;

import java.util.List;
import java.util.UUID;

@Collection("mapkey")
public interface MapKeyRepository extends Repository<MapKey, UUID> {

    List<MapKey> findManyByType(Type type);

    MapKey findFirstByAddressAndAddressBannedIsTrue(String address);

    MapKey findFirstByAddressBannedIsTrueAndAddress(String address);

    MapKey findFirstByUniqueIdAndType(UUID uuid, Type type);
}
