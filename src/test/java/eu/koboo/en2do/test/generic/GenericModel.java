package eu.koboo.en2do.test.generic;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public abstract class GenericModel<T> {

    @Id // en2do
    UUID uniqueId;

    T property;
}
