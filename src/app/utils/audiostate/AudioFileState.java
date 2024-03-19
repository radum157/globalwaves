package app.utils.audiostate;

import lombok.Getter;
import lombok.Setter;

/**
 * Class describing the state of an audio file inside a media player.
 */
@Getter
@Setter
public class AudioFileState {
    private int lastIndex;
    private int timePosition;
    private int lastDuration;

    private int repeatMode;
    private String repeatModeAsString;

    private boolean isOver;

    /**
     * Sets state variables
     *
     * @param index    the last index of the collection
     * @param position the time the player left off
     * @param duration the duration of the last item in the collection
     */
    public void setState(final int index, final int position, final int duration) {
        lastIndex = index;
        timePosition = position;
        lastDuration = duration;
    }

    /**
     * Sets state variables
     *
     * @param fileEnded if the file is over
     */
    public void setState(final int index, final int position,
                         final int duration, final boolean fileEnded) {
        setState(index, position, duration);
        isOver = fileEnded;
    }

    /**
     * Clears the state of the file
     */
    public void clear() {
        setState(0, 0, 0);
        repeatMode = 0;
        isOver = true;
    }

    /**
     * @return The time remaining until the end of the file
     */
    public int getTimeRemaining() {
        return lastDuration - timePosition;
    }

    /**
     * Basic audio files can not be shuffled
     *
     * @return false
     */
    public boolean isShuffled() {
        return false;
    }
}
