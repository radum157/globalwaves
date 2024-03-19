package app.utils.tie;

/**
 * Tied entity interface. Ties control whether an entity can be deleted.
 */
public interface TiedEntity {
    /**
     * Adds a new tie
     */
    void addTie();

    /**
     * Removes an existing tie
     */
    void removeTie();
}
