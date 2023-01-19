package eu.koboo.en2do.test.alien;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class Alien {

    @Id // en2do
    UUID uniqueId;

    Map<Long, String> ufoIdList;
    Map<Planet, Long> planetTimeMap;
    Map<String, Planet> translationPlanetMap;
}
