package observer;

import java.util.LinkedList;
import java.util.List;

/**
 * Observable abstract class. Stores observers and can send notifications.
 */
public final class Observable {
    private final List<Observer> observers = new LinkedList<>();

    /**
     * Sends a notification to all observers.
     * Uses the type parameter directly to indicate what new object
     * when passing the notification.
     *
     * @param type   the notification type
     * @param author the author of the notification
     */
    public void notifyAll(final String type, final String author) {
        String message = "New " + type + " from " + author + ".";
        observers.forEach(observer -> observer.notify(message));
    }

    /**
     * Adds a new observer to the list or removes it if it exists
     *
     * @param observer the observer
     * @return True if an observer was added, false otherwise
     */
    public boolean addObserver(final Observer observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
            return false;
        }
        return observers.add(observer);
    }
}
