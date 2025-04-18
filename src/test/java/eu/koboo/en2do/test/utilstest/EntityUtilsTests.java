package eu.koboo.en2do.test.utilstest;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.customer.Customer;
import eu.koboo.en2do.test.customerextended.CustomerExtended;
import eu.koboo.en2do.utility.EntityUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityUtilsTests {

    @Test
    public void testEntityUtils() {
        Customer original = Const.createCustomer();
        CustomerExtended copy = new CustomerExtended();

        EntityUtils.copyProperties(original, copy);

        assertEquals(original.getUniqueId(), copy.getUniqueId());
        assertEquals(original.getFirstName(), copy.getFirstName());
        assertEquals(original.getLastName(), copy.getLastName());
    }
}
