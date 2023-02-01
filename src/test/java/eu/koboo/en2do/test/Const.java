package eu.koboo.en2do.test;

import eu.koboo.en2do.test.alien.Alien;
import eu.koboo.en2do.test.alien.Planet;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customer.CustomerType;
import eu.koboo.en2do.test.customer.Order;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Const {

    public static final UUID UNIQUE_ID = UUID.fromString("2ce67956-7211-4fec-a7ad-b24f2e355b61");
    public static final int CUSTOMER_ID = 1;
    public static final String FIRST_NAME = "Rainer";
    public static final String LAST_NAME = "Zufall";
    public static final String BIRTHDAY = "12.12.2012";
    public static final String STREET = "Backweg";
    public static final int HOUSE_NUMBER = 12;
    public static final int POSTAL_CODE = 12122;
    public static final String CITY = "Zufallsort";
    public static final long PHONE_NUMBER = 4915122334455L;
    public static final double BALANCE = 543.21;
    public static final List<Order> ORDERS = Arrays.asList(
            new Order("First", 1.0, Arrays.asList(1, 2, 3, 4)),
            new Order("Second", 2.0, Arrays.asList(1, 2, 3, 4)),
            new Order("Third", 3.0, Arrays.asList(1, 2, 3, 4))
    );
    public static final CustomerType TYPE = CustomerType.DEFAULT;

    public static @NotNull Customer createNewCustomer() {
        Customer customer = new Customer();
        customer.setUniqueId(UNIQUE_ID);
        customer.setCustomerId(CUSTOMER_ID);
        customer.setFirstName(FIRST_NAME);
        customer.setLastName(LAST_NAME);
        customer.setBirthday(BIRTHDAY);
        customer.setStreet(STREET);
        customer.setHouseNumber(HOUSE_NUMBER);
        customer.setPostalCode(POSTAL_CODE);
        customer.setCity(CITY);
        customer.setPhoneNumber(PHONE_NUMBER);
        customer.setBalance(BALANCE);
        customer.setOrders(ORDERS);
        customer.setCustomerType(TYPE);
        return customer;
    }

    public static @NotNull Alien createNewAlien() {
        Alien alien = new Alien();
        alien.setUniqueId(UNIQUE_ID);
        alien.setUfoIdList(Map.of(10L, "Millenium Falcon", 20L, "X-WING", 30L, "Death-Star"));
        alien.setPlanetTimeMap(Map.of(Planet.NEPTUNE, 100L, Planet.EARTH, 0L, Planet.MARS, 3000L));
        alien.setTranslationPlanetMap(Map.of("Neptun", Planet.NEPTUNE, "Erde", Planet.EARTH, "Mars", Planet.MARS));
        return alien;
    }
}
