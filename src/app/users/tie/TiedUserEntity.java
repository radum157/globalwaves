package app.users.tie;

import app.utils.tie.TiedEntity;
import databases.Library;
import fileio.input.UserInput;
import lombok.Getter;

/**
 * Base user class. Wrapper for UserInput.
 * <p>
 * All users have different ties in the library, hence TiedEntity.
 */
@Getter
public abstract class TiedUserEntity implements TiedEntity {
    private final UserInput userInput;
    private int ties;

    public TiedUserEntity(final UserInput userInput) {
        this.userInput = userInput;
    }

    @Override
    public final void addTie() {
        ties++;
    }

    @Override
    public final void removeTie() {
        ties = Math.max(0, ties - 1);
    }

    /**
     * Removes all tieable objects associated with the user from a library
     *
     * @param library the library instance
     */
    public abstract void untieFrom(Library library);

    /**
     * @return The username
     */
    public String getName() {
        return userInput.getUsername();
    }

    /**
     * @return The city of the user
     */
    public String getCity() {
        return userInput.getCity();
    }

    /**
     * @return The age of the user
     */
    public int getAge() {
        return userInput.getAge();
    }
}
