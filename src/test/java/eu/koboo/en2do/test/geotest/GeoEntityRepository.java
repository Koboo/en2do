package eu.koboo.en2do.test.geotest;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.geo.Geo;

import java.util.UUID;

@Collection("geo_entities")
public interface GeoEntityRepository extends Repository<GeoEntity, UUID> {

    GeoEntity findFirstByPointGeo(Geo geo);
}
