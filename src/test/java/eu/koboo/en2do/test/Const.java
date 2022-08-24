package eu.koboo.en2do.test;

import eu.koboo.en2do.test.customer.Customer;

import java.util.*;

public class Const {

    public static final Customer CUSTOMER;

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
    public static final List<Integer> ORDER_NUMBERS = Arrays.asList(1, 2, 3, 4, 5);
    public static final Map<String, String> ORDER_TEXTS = new HashMap<>() {{
        put("1", "First");
        put("2", "Second");
        put("3", "Third");
        put("4", "Fourth");
        put("5", "Fifth");
    }};

    static {
        CUSTOMER = new Customer();
        CUSTOMER.setUniqueId(UNIQUE_ID);
        CUSTOMER.setCustomerId(CUSTOMER_ID);
        CUSTOMER.setFirstName(FIRST_NAME);
        CUSTOMER.setLastName(LAST_NAME);
        CUSTOMER.setBirthday(BIRTHDAY);
        CUSTOMER.setStreet(STREET);
        CUSTOMER.setHouseNumber(HOUSE_NUMBER);
        CUSTOMER.setPostalCode(POSTAL_CODE);
        CUSTOMER.setCity(CITY);
        CUSTOMER.setPhoneNumber(PHONE_NUMBER);
        CUSTOMER.setBalance(BALANCE);
        CUSTOMER.setOrderNumbers(ORDER_NUMBERS);
        CUSTOMER.setOrderTexts(ORDER_TEXTS);
    }
}