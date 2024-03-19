package factory;

/**
 * Base Factory class.
 *
 * @param <T> the type of object to be created
 */
public interface Factory<T> {
    /**
     * Generic factory object creation
     *
     * @param data the object data (for the constructor)
     * @return The created object
     */
    T createObject(Object[] data);
}
