package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.Parameter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Apprentissage des tolérances d'une recherche dont on connaît le nombre de résultats attendu : chaque combinaison
 * (tolérance de couleur, taux d'erreur) est essayée sur des captures réelles, et l'on retient celle qui trouve le plus
 * souvent exactement le bon nombre d'objets.
 * <p>
 * Des statistiques plutôt qu'une élimination : une capture ratée (objet masqué un instant) n'écarte pas définitivement une
 * bonne combinaison, elle baisse seulement son taux de réussite.
 */
public final class ParameterLearning {

    /**
     * Essais et réussites d'une combinaison.
     */
    public record Stat(int trials, int successes) {

        public static final Stat NONE = new Stat(0, 0);

        /**
         * Taux de réussite lissé (règle de succession de Laplace) : une combinaison essayée une fois avec succès (2/3) ne
         * passe pas devant une combinaison réussie 20 fois sur 21 (0,91).
         */
        public double score() {

            return (successes + 1.0) / (trials + 2.0);
        }

        public Stat plus(boolean success) {

            return new Stat(trials + 1, successes + (success ? 1 : 0));
        }
    }

    /**
     * Combinaisons essayées : tolérance de couleur 0 à 50 par pas de 5, taux d'erreur 0 à 30 % par pas de 5 %.
     */
    public static final List<Parameter> CANDIDATES = candidates();

    /**
     * Score en dessous duquel une combinaison n'est pas retenue, même la meilleure : elle se trompe plus d'une fois sur
     * deux.
     */
    static final double MIN_SCORE = 0.5;

    private ParameterLearning() {}

    private static List<Parameter> candidates() {

        List<Parameter> candidates = new ArrayList<>();
        for (int precision = 0; precision <= 50; precision += 5) {
            for (int errorRate = 0; errorRate <= 30; errorRate += 5) {
                candidates.add(new Parameter(precision, errorRate / 100.0));
            }
        }
        return List.copyOf(candidates);
    }

    /**
     * Combinaisons à essayer sur la prochaine capture : d'abord celles jamais essayées, puis les moins essayées (pour
     * affiner les statistiques), et toujours la meilleure actuelle (pour vérifier qu'elle tient).
     *
     * @param limit nombre de combinaisons au plus : chacune coûte un parcours de la capture
     */
    public static List<Parameter> nextTrials(Map<Parameter, Stat> stats, int limit) {

        List<Parameter> trials = new ArrayList<>();
        best(stats).ifPresent(trials::add);
        CANDIDATES.stream()
                  .filter(candidate -> !trials.contains(candidate))
                  .sorted(Comparator.comparingInt((Parameter candidate) -> stats.getOrDefault(candidate, Stat.NONE).trials())
                                    .thenComparing(candidate -> -stats.getOrDefault(candidate, Stat.NONE).score()))
                  .limit(Math.max(0, limit - trials.size()))
                  .forEach(trials::add);
        return trials;
    }

    /**
     * Meilleure combinaison : le plus haut taux de réussite lissé ; à égalité, la plus essayée (la plus sûre), puis la plus
     * stricte (moins de faux positifs le jour où l'écran change).
     *
     * @return vide si aucune combinaison n'a encore fait ses preuves (score d'au moins {@value #MIN_SCORE})
     */
    public static Optional<Parameter> best(Map<Parameter, Stat> stats) {

        return stats.entrySet().stream()
                    .filter(entry -> entry.getValue().successes() > 0 && entry.getValue().score() >= MIN_SCORE)
                    .max(Comparator.comparingDouble((Map.Entry<Parameter, Stat> entry) -> entry.getValue().score())
                                   .thenComparingInt(entry -> entry.getValue().trials())
                                   .thenComparingDouble(entry -> -entry.getKey().errorRate())
                                   .thenComparingInt(entry -> -entry.getKey().precision()))
                    .map(Map.Entry::getKey);
    }
}
