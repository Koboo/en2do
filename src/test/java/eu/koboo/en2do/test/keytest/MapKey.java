package eu.koboo.en2do.test.keytest;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapKey {

    @Id
    UUID uniqueId;

    String address;
    boolean addressBanned;
    Type type;

    Map<String, String> stringMap;
    Map<Boolean, String> booleanMap;
    Map<Double, String> doubleMap;
    Map<UUID, String> uuidMap;
    Map<SomeEnum, String> enumMap;
}
