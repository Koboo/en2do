package eu.koboo.en2do.test.geotest;

import com.mongodb.client.model.geojson.Point;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.compound.GeoIndex;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class GeoEntity {

    @Id
    UUID identifier;

    @GeoIndex
    Point point;
}