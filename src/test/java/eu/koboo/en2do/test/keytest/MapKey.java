package eu.koboo.en2do.test.keytest;

import eu.koboo.en2do.repository.entity.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MapKey {

    @Id
    UUID uniqueId;

    Map<String, String> stringMap;
    Map<Boolean, String> booleanMap;
    Map<Double, String> doubleMap;
    Map<UUID, String> uuidMap;
    Map<SomeEnum, String> enumMap;
}
