package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParameterLearningTest {

    private static ParameterLearning.Stat stat(int trials, int successes) {

        return new ParameterLearning.Stat(trials, successes);
    }

    @Test
    void candidatesCoverTheGridWithoutFloatingPointNoise() {

        assertEquals(11 * 7, ParameterLearning.CANDIDATES.size());
        assertTrue(ParameterLearning.CANDIDATES.contains(new Parameter(15, 0.15)), "0.05 × 3 arrondi : 0.15, pas 0.15000000000000002");
        assertTrue(ParameterLearning.CANDIDATES.contains(new Parameter(50, 0.30)));
    }

    @Test
    void oneMissDoesNotDiscardAReliableParameter() {

        Map<Parameter, ParameterLearning.Stat> stats = new HashMap<>();
        stats.put(new Parameter(20, 0.05), stat(20, 19));     // une capture ratée (objet masqué)
        stats.put(new Parameter(0, 0.0), stat(1, 1));         // une seule réussite : pas encore fiable
        stats.put(new Parameter(50, 0.30), stat(20, 2));      // trop permissive : faux positifs

        assertEquals(new Parameter(20, 0.05), ParameterLearning.best(stats).orElseThrow());
    }

    @Test
    void amongEquallyReliableParametersTheStrictestWins() {

        Map<Parameter, ParameterLearning.Stat> stats = new HashMap<>();
        stats.put(new Parameter(30, 0.10), stat(10, 10));
        stats.put(new Parameter(20, 0.05), stat(10, 10));
        stats.put(new Parameter(20, 0.10), stat(10, 10));

        assertEquals(new Parameter(20, 0.05), ParameterLearning.best(stats).orElseThrow());
    }

    @Test
    void noBestWhileNothingHasProvenItself() {

        assertTrue(ParameterLearning.best(Map.of()).isEmpty());
        assertTrue(ParameterLearning.best(Map.of(new Parameter(10, 0.05), stat(10, 1))).isEmpty(), "réussit une fois sur dix");
    }

    @Test
    void trialsStartWithTheBestThenTheLeastTried() {

        Map<Parameter, ParameterLearning.Stat> stats = new HashMap<>();
        for (Parameter candidate : ParameterLearning.CANDIDATES) {stats.put(candidate, stat(3, 0));}
        stats.put(new Parameter(25, 0.10), stat(3, 3));
        stats.put(new Parameter(5, 0.0), stat(0, 0));

        List<Parameter> trials = ParameterLearning.nextTrials(stats, 4);

        assertEquals(4, trials.size());
        assertEquals(new Parameter(25, 0.10), trials.get(0), "la meilleure est revérifiée");
        assertEquals(new Parameter(5, 0.0), trials.get(1), "puis celle jamais essayée");
        assertEquals(4, trials.stream().distinct().count());
    }

    @Test
    void everyCandidateIsEventuallyTried() {

        Map<Parameter, ParameterLearning.Stat> stats = new HashMap<>();
        for (int round = 0; round < 10; round++) {
            for (Parameter trial : ParameterLearning.nextTrials(stats, 8)) {stats.merge(trial, stat(1, 0), (a, b) -> a.plus(false));}
        }
        assertEquals(ParameterLearning.CANDIDATES.size(), stats.size(), "77 combinaisons en 10 recherches de 8 essais");
    }
}
