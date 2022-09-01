package eu.koboo.en2do.test.customer;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@AllArgsConstructor // lombok - not required
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class Order {

    String orderText;
    double orderPrice;
    List<Integer> positionIds;
}