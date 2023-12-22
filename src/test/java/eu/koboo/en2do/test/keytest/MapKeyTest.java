package eu.koboo.en2do.test.keytest;

import eu.koboo.en2do.test.RepositoryTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.bson.assertions.Assertions.assertNotNull;

public class MapKeyTest extends RepositoryTest<MapKey, UUID, MapKeyRepository> {

    @Override
    public Class<MapKeyRepository> repositoryClass() {
        return MapKeyRepository.class;
    }

    @Test
    public void testCreate() {
        MapKey mapKey = new MapKey();
        UUID uniqueId = UUID.randomUUID();
        mapKey.setUniqueId(uniqueId);
        mapKey.setStringMap(Map.of("Test", "Test"));
        mapKey.setBooleanMap(Map.of(true, "Test"));
        mapKey.setDoubleMap(Map.of(1.234, "Test"));
        mapKey.setUuidMap(Map.of(UUID.randomUUID(), "Test"));
        mapKey.setEnumMap(Map.of(SomeEnum.ONE, "Test"));
        repository.save(mapKey);

        mapKey = repository.findFirstById(uniqueId);
        assertNotNull(mapKey);
        assertNotNull(mapKey.getStringMap());
        assertNotNull(mapKey.getBooleanMap());
        assertNotNull(mapKey.getDoubleMap());
        assertNotNull(mapKey.getUuidMap());
        assertNotNull(mapKey.getEnumMap());
        repository.drop();
    }
}
