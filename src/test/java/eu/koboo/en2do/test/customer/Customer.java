package eu.koboo.en2do.test.customer;

import eu.koboo.en2do.annotation.Entity;
import eu.koboo.en2do.annotation.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity("Customers")
public class Customer {

    @Id
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