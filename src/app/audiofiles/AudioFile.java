package app.audiofiles;

import app.users.normal.User;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.audio.PlaylistConstants;
import app.utils.constants.audio.PodcastConstants;
import app.utils.constants.users.UserConstants;
import app.utils.tie.TiedEntity;

import java.util.List;

/**
 * Interface for all audio file types
 */
public interface AudioFile extends TiedEntity {
    /**
     * @return The name of the audio file
     */
    String getName();

    /**
     * @return The currently playing file's creator's name
     */
    String getFileOwner(AudioFileState currentState);

    /**
     * @return If the file is an empty collection
     */
    boolean isEmpty();

    /**
     * @return If the file can be added to a personal collection (playlist)
     */
    default boolean canAddToCollection() {
        return false;
    }

    /**
     * Follows the file if it is a playlist
     *
     * @return A message corresponding to the success state, as defined in PlaylistConstants
     */
    default String addFollow() {
        return PlaylistConstants.FOLLOW_NOT_PLAYLIST;
    }

    /**
     * Unfollows the file
     *
     * @return Same as for addFollow
     */
    default String removeFollow() {
        return PlaylistConstants.FOLLOW_NOT_PLAYLIST;
    }

    /**
     * Simulates the passage of time and updates the given starting file state
     *
     * @param currentState the state of the audio file
     * @param elapsedTime  the elapsed time
     */
    void simulateTime(AudioFileState currentState, int elapsedTime, User user);

    /**
     * @return A state variable representing the initial state
     */
    AudioFileState getInitialState();

    /**
     * Skips to the next file and returns its name
     *
     * @param currentState the starting state
     * @return The name of the next file in the collection
     */
    default String getNext(final AudioFileState currentState, final User user) {
        simulateTime(currentState, currentState.getTimeRemaining(), user);
        return getCurrentName(currentState);
    }

    /**
     * Sets the state to the previous source
     *
     * @param currentState the starting state
     * @return The name of the previous file in the collection
     */
    String getPrevious(AudioFileState currentState, User user);

    /**
     * @param repeatMode the repeat mode
     * @return The repeat mode as a string as defined in MediaPlayerConstants
     */
    String getRepeatModeAsString(int repeatMode);

    /**
     * Likes the file if it is a song
     *
     * @return A message corresponding to the success status, as defined in UserConstants
     */
    default String addLike() {
        return UserConstants.LIKE_NOT_SONG;
    }

    /**
     * Unlikes the file
     *
     * @return Same as for addLike
     */
    default String removeLike() {
        return UserConstants.LIKE_NOT_SONG;
    }

    /**
     * Shuffles the file if it is a playlist
     *
     * @param seed the seed of the random indexes
     * @return The shuffle indexes or null
     */
    default List<Integer> shuffleFile(final Integer seed) {
        return null;
    }

    /**
     * Attempts to perform a skip of 90 seconds
     *
     * @return If it succeeded
     */
    default String skipForward(final AudioFileState currentState, final User user) {
        return PodcastConstants.SKIP_NOT_PODCAST;
    }

    /**
     * Attempts to perform a rewind of 90 seconds
     *
     * @return If it succeeded
     */
    default String skipBackward(final AudioFileState currentState) {
        return PodcastConstants.SKIP_NOT_PODCAST;
    }

    /**
     * @param currentState the current state of the file
     * @return The name of the currently playing file in the collection
     */
    default String getCurrentName(final AudioFileState currentState) {
        return (currentState.isOver()) ? null : getCurrentFile(currentState).getName();
    }

    /**
     * @param currentState the current state of the file
     * @return The currently playing file in the collection
     */
    default AudioFile getCurrentFile(final AudioFileState currentState) {
        return this;
    }
}
