package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.annotation.Entity;
import eu.koboo.en2do.annotation.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter // lombok
@Setter // lombok
@NoArgsConstructor // lombok
@FieldDefaults(level = AccessLevel.PRIVATE) // lombok
@ToString
@Entity("Customers") // en2do
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
    List<Integer> orderNumbers;
    Map<String, String> orderTexts;
}