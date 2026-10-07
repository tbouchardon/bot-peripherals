package fr.ksuto.prh.tools;

import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.helpers.ParameterLearning;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class SearchMemoryTest {

    @TempDir
    Path folder;

    @Test
    void parameterStatsAccumulateAcrossSearches() throws Exception {

        try (SearchMemory memory = SearchMemory.open(folder.resolve("prh.db"))) {
            Map<Parameter, Boolean> first = new LinkedHashMap<>();
            first.put(new Parameter(20, 0.05), true);
            first.put(new Parameter(0, 0.0), false);
            memory.recordTrials("bouton@1920x1080", first);
            memory.recordTrials("bouton@1920x1080", Map.of(new Parameter(20, 0.05), false));

            Map<Parameter, ParameterLearning.Stat> stats = memory.parameterStats("bouton@1920x1080");
            assertEquals(new ParameterLearning.Stat(2, 1), stats.get(new Parameter(20, 0.05)));
            assertEquals(new ParameterLearning.Stat(1, 0), stats.get(new Parameter(0, 0.0)));
            assertTrue(memory.parameterStats("autre@1920x1080").isEmpty(), "rangé par recherche");
        }
    }

    @Test
    void persistsInItsFile() throws Exception {

        Path file = folder.resolve("sous-dossier").resolve("prh.db");
        try (SearchMemory memory = SearchMemory.open(file)) {
            memory.setArea("bouton@800x600", new Rectangle(10, 20, 30, 40));
        }
        try (SearchMemory reopened = SearchMemory.open(file)) {
            assertEquals(new Rectangle(10, 20, 30, 40), reopened.area("bouton@800x600").orElseThrow());
        }
    }

    @Test
    void keepsOnlyTheMostRecentPositions() {

        try (SearchMemory memory = SearchMemory.inMemory()) {
            memory.addPositions("k", List.of(new Point(1, 1), new Point(2, 2)), 3);
            memory.addPositions("k", List.of(new Point(3, 3), new Point(4, 4)), 3);

            assertEquals(List.of(new Point(2, 2), new Point(3, 3), new Point(4, 4)), memory.positions("k"));
        }
    }

    @Test
    void clearingForgetsAreaPositionsAndParameters() {

        try (SearchMemory memory = SearchMemory.inMemory()) {
            memory.setArea("k", new Rectangle(0, 0, 5, 5));
            memory.addPositions("k", List.of(new Point(1, 1)), 10);
            memory.recordTrials("k", Map.of(new Parameter(10, 0.0), true));

            memory.clearArea("k");
            assertTrue(memory.area("k").isEmpty());
            assertTrue(memory.positions("k").isEmpty());
            assertFalse(memory.parameterStats("k").isEmpty(), "les tolérances restent");

            memory.clear("k");
            assertTrue(memory.parameterStats("k").isEmpty());
        }
    }
}
