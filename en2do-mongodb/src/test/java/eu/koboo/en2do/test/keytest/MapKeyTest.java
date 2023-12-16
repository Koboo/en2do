package eu.koboo.en2do.test.keytest;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.MongoSettings;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

public class MapKeyTest {

    static MongoManager mongoManager = new MongoManager();

    @Test
    public void testCreate() {
        MapKeyRepository mapKeyRepository = mongoManager.create(MapKeyRepository.class);
        MapKey mapKey = new MapKey();
        UUID uniqueId = UUID.randomUUID();
        mapKey.setUniqueId(uniqueId);
        mapKey.setStringMap(Map.of("Test", "Test"));
        mapKey.setBooleanMap(Map.of(true, "Test"));
        mapKey.setDoubleMap(Map.of(1.234, "Test"));
        mapKey.setUuidMap(Map.of(UUID.randomUUID(), "Test"));
        mapKey.setEnumMap(Map.of(SomeEnum.ONE, "Test"));
        mapKeyRepository.save(mapKey);

        mapKey = mapKeyRepository.findFirstById(uniqueId);
        System.out.println(mapKey);
    }
}
