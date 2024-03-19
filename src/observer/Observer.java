package observer;

/**
 * Observer interface
 */
public interface Observer {
    /**
     * Receive a notification
     *
     * @param message the notification message
     */
    void notify(String message);

    /**
     * Starts observing an object
     *
     * @param observable the observable object
     * @return The result of Observable.addObserver
     */
    default boolean subscribeTo(final Observable observable) {
        return observable.addObserver(this);
    }
}
