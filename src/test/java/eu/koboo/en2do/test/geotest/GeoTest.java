package eu.koboo.en2do.test.geotest;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import eu.koboo.en2do.repository.methods.geo.Geo;
import eu.koboo.en2do.test.RepositoryTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeoTest extends RepositoryTest<GeoEntity, UUID, GeoEntityRepository> {

    @Override
    public Class<GeoEntityRepository> repositoryClass() {
        return GeoEntityRepository.class;
    }

    @Test
    public void test() {
        GeoEntity entity = new GeoEntity();
        entity.setIdentifier(UUID.randomUUID());
        entity.setPoint(new Point(new Position(51.186266, 10.047910)));

        repository.save(entity);

        GeoEntity first = repository.findFirstByPointGeo(Geo.of()
            .coordinates(51.182230604432114, 10.040056400797514)
            .maxDistance(1000D));

        // 720m
        //Point point = new Point(new Position(10.040056400797514, 51.182230604432114));
//        Point point = new Point(new Position(51.182230604432114, 10.040056400797514));
//
//        Bson filter = Filters.near("point", point, 1000d, null);
//        FindIterable<GeoEntity> entities = collection.find(filter);
//        GeoEntity first = entities.first();

        assertNotNull(first);
        assertEquals(entity.getIdentifier(), first.getIdentifier());
    }
}
