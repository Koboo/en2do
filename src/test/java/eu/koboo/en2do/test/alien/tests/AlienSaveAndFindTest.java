package eu.koboo.en2do.test.alien.tests;

import eu.koboo.en2do.test.Const;
import eu.koboo.en2do.test.alien.Alien;
import eu.koboo.en2do.test.alien.AlienRepositoryTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlienSaveAndFindTest extends AlienRepositoryTest {

    @Test
    @Order(1)
    public void cleanUpRepository() {
        List<Alien> customerList = repository.findAll();
        assertNotNull(customerList);
        assertTrue(customerList.isEmpty());
    }

    @Test
    @Order(2)
    public void saveCustomer() {
        Alien alien = Const.createNewAlien();
        assertNotNull(alien);
        assertFalse(repository.exists(alien));
        assertTrue(repository.save(alien));
        assertTrue(repository.exists(alien));
    }

    @Test
    @Order(3)
    public void findCustomer() {
        assertTrue(repository.existsById(Const.UNIQUE_ID));
        Alien alien = repository.findById(Const.UNIQUE_ID);
        assertNotNull(alien);
        assertEquals(3, alien.getUfoIdList().size());
        assertEquals(3, alien.getPlanetTimeMap().size());
        assertEquals(3, alien.getTranslationPlanetMap().size());
    }
}
