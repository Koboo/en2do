package eu.koboo.en2do.test.generic;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class GenericModelImpl extends GenericModel<String> {

}
