package eu.koboo.en2do.test.mapkeys;

import eu.koboo.en2do.test.RepositoryTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapKeyTest extends RepositoryTest<MapKey, UUID, MapKeyRepository> {

    @Override
    public Class<MapKeyRepository> repositoryClass() {
        return MapKeyRepository.class;
    }

    @Test
    public void testMapKeys() {
        MapKey mapKey = new MapKey();
        UUID uniqueId = UUID.randomUUID();
        mapKey.setUniqueId(uniqueId);
        mapKey.setStringMap(Map.of("Test", "Test"));
        mapKey.setBooleanMap(Map.of(true, "Test"));
        mapKey.setDoubleMap(Map.of(1.234, "Test"));
        UUID uuidKey = UUID.randomUUID();
        mapKey.setUuidMap(Map.of(uuidKey, "Test"));
        mapKey.setEnumMap(Map.of(MapKeyEnum.ONE, "Test"));
        assertTrue(repository.save(mapKey));

        mapKey = repository.findFirstById(uniqueId);
        assertNotNull(mapKey);
        assertNotNull(mapKey.getStringMap());
        assertTrue(mapKey.getStringMap().containsKey("Test"));
        assertNotNull(mapKey.getBooleanMap());
        assertTrue(mapKey.getBooleanMap().containsKey(true));
        assertNotNull(mapKey.getDoubleMap());
        assertTrue(mapKey.getDoubleMap().containsKey(1.234));
        assertNotNull(mapKey.getUuidMap());
        assertTrue(mapKey.getUuidMap().containsKey(uuidKey));
        assertNotNull(mapKey.getEnumMap());
        assertTrue(mapKey.getEnumMap().containsKey(MapKeyEnum.ONE));
        assertTrue(repository.drop());
    }
}
