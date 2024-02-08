package eu.koboo.en2do.test.geotest;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import eu.koboo.en2do.repository.methods.geo.Geo;
import eu.koboo.en2do.test.RepositoryTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GeoTest extends RepositoryTest<GeoEntity, UUID, GeoEntityRepository> {

    static final GeoEntity ENTITY;

    static {
        ENTITY = new GeoEntity();
        ENTITY.setIdentifier(UUID.randomUUID());
        ENTITY.setPoint(new Point(new Position(10.047910, 51.186266)));
    }

    @Override
    public Class<GeoEntityRepository> repositoryClass() {
        return GeoEntityRepository.class;
    }

    @Test
    public void getByMaxDistanceSuccess() {
        repository.save(ENTITY);

        GeoEntity first = repository.findFirstByPointGeo(Geo.of()
            .coordinates(51.182230604432114, 10.040056400797514)
            .maxDistance(710));

        assertNotNull(first);
        assertEquals(ENTITY.getIdentifier(), first.getIdentifier());
        assertTrue(repository.delete(first));
        assertFalse(repository.exists(first));
    }

    @Test
    public void getByMaxDistanceFailure() {
        repository.save(ENTITY);

        GeoEntity first = repository.findFirstByPointGeo(Geo.of()
            .coordinates(51.182230604432114, 10.040056400797514)
            .maxDistance(700));

        assertNull(first);
        assertTrue(repository.delete(ENTITY));
        assertFalse(repository.exists(ENTITY));
    }

    @Test
    public void getByMinDistanceSuccess() {
        repository.save(ENTITY);

        GeoEntity first = repository.findFirstByPointGeo(Geo.of()
            .coordinates(51.182230604432114, 10.040056400797514)
            .minDistance(700));

        assertNotNull(first);
        assertEquals(ENTITY.getIdentifier(), first.getIdentifier());
        assertTrue(repository.delete(first));
        assertFalse(repository.exists(first));
    }

    @Test
    public void getByMinDistanceFailure() {
        repository.save(ENTITY);

        GeoEntity first = repository.findFirstByPointGeo(Geo.of()
            .coordinates(51.182230604432114, 10.040056400797514)
            .minDistance(710));

        assertNull(first);
        assertTrue(repository.delete(ENTITY));
        assertFalse(repository.exists(ENTITY));
    }
}
