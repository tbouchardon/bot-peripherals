package fr.ksuto.prh.entities;

/**
 * Tolérances d'une recherche d'image.
 *
 * @param precision tolérance de couleur par canal (0 = pixel identique ; plus la valeur est haute, plus la recherche est
 *                  permissive), de préférence &lt; 100
 * @param errorRate part des pixels de l'image de référence qui peuvent différer, de 0.0 à 1.0
 */
public record Parameter(int precision, double errorRate) {

    public Parameter {

        // Arrondi au centième : les pas de 0.05 additionnés ne tombent pas juste en virgule flottante (0.15000000000000002)
        errorRate = Math.round(errorRate * 100) / 100.0;
    }
}
