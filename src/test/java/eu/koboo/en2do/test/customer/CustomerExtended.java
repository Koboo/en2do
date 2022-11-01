package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.index.EntityIndex;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
@EntityIndex(value = {"orderStatus", "lockStatus"}, ascending = false) // en2do
@EntityIndex(value = {"customerId"}) // en2do
@EntityIndex(value = {"firstName", "lastName"}, ascending = true) // en2do
public class CustomerExtended extends Customer {

    String orderStatus;
    String lockStatus;
}
