package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.index.Id;
import eu.koboo.en2do.index.ttl.TTLIndex;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString // lombok
@TTLIndex(value = "createTime", ttl = 10) // en2do - Expires 10 seconds after create date
@TTLIndex(value = "expireTime") //  en2do - Expires on "expireDate" clock time
public class Customer {

    @Id // en2do
    UUID uniqueId;

    int customerId;
    String firstName;
    String lastName;
    String birthday;
    String street;
    int houseNumber;
    Integer postalCode;
    String city;
    Long phoneNumber;
    double balance;
    List<Order> orders;
    CustomerType customerType;
    Date createTime;
    Date expireTime;
}
