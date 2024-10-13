package eu.koboo.en2do.test.customer;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@AllArgsConstructor // lombok - not required
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
public class Order {

    private static final Random RANDOM = new Random();

    String orderText;
    double orderPrice;
    List<Integer> positionIds;

    public static Order create(double price, String text) {
        return new Order() {{
            setOrderPrice(price);
            setOrderText(text);
            List<Integer> positionIds = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                positionIds.add(RANDOM.nextInt());
            }
            setPositionIds(positionIds);
        }};
    }
}