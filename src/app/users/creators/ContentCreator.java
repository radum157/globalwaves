package app.users.creators;

import app.page.Page;
import app.users.tie.TiedUserEntity;
import fileio.input.UserInput;
import observer.Observable;
import observer.Observer;

/**
 * Content creator base class
 */
public abstract class ContentCreator extends TiedUserEntity {
    /* Composition to solve multiple extension */
    private final Observable observableData = new Observable();

    public ContentCreator(final UserInput userInput) {
        super(userInput);
    }

    /**
     * @return The correlated page
     */
    public abstract Page getPage();

    /**
     * Calls notifyAll on the observable data
     *
     * @param type the notification type
     */
    public void notifyAll(final String type) {
        observableData.notifyAll(type, getName());
    }

    /**
     * Calls addObserver on the observable data
     *
     * @param observer the observer
     * @return What addObserver returns in Observable
     */
    public boolean addObserver(final Observer observer) {
        return observableData.addObserver(observer);
    }
}
