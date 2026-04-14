package service.route;

/**
 * RouteStrategy
 * Rota optimizasyonu icin 3 farkli strateji.
 *
 * CHEAPEST          -> her urunu en ucuz marketten al
 * FEWEST_STOPS      -> mumkun oldugunca az markete git
 * SHORTEST_DISTANCE -> en kisa yurume mesafesini sec
 */
public enum RouteStrategy {
    CHEAPEST,
    FEWEST_STOPS,
    SHORTEST_DISTANCE
}
