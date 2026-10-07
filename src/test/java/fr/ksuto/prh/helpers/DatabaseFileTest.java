package fr.ksuto.prh.helpers;

import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseFileTest {

    private static Path file(String value) {

        Properties properties = new Properties();
        if (value != null) {properties.setProperty("ksuto.prh.database.file", value);}
        return AbstractSeeker.databaseFile(properties);
    }

    @Test
    void tildeIsTheUserHomeOnEverySystem() {

        Path home = Path.of(System.getProperty("user.home"));

        assertEquals(home.resolve(".ksuto").resolve("prh.db"), file(null), "par défaut");
        assertEquals(home.resolve("bots").resolve("prh.db"), file("~/bots/prh.db"));
        assertEquals(home.resolve("bots").resolve("prh.db"), file("~\\bots\\prh.db".replace('\\', java.io.File.separatorChar)));
        assertEquals(Path.of("donnees", "prh.db"), file("donnees/prh.db"), "chemin relatif inchangé");
    }
}
