package eu.koboo.en2do.test.customer;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class CustomerExtended extends Customer {

    String orderStatus;
    String lockStatus;
}
