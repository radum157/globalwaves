package app.mediaplayer;

import app.admin.Admin;
import app.audiofiles.Song;
import app.audiofiles.collections.Podcast;
import app.users.creators.ContentCreator;
import app.utils.audiostate.PlaylistState;
import app.utils.audiostate.PodcastState;
import databases.UserDatabase;
import lombok.Getter;
import app.audiofiles.AudioFile;
import fileio.commands.Command;
import app.users.normal.User;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.player.MediaPlayerConstants;
import app.utils.constants.audio.PlaylistConstants;
import app.utils.constants.audio.PodcastConstants;
import app.utils.constants.users.UserConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads audio files and simulates all operations that can be made on them.
 * Timestamps are used in most functions to update the player status
 */
@Getter
public final class MediaPlayer {
    private final User playerOwner;

    /* Player data */
    private int lastUpdateTime;
    private boolean isPlaying;
    private boolean isOffline;

    private final List<PodcastState> podcastStates = new ArrayList<>();

    private AudioFile audioFile;
    private AudioFileState fileState = new AudioFileState();

    private int adTime;
    private double adPrice;

    public MediaPlayer(final User user) {
        fileState.setRepeatModeAsString(MediaPlayerConstants.NO_REPEAT_MESSAGE);
        fileState.setOver(true);

        playerOwner = user;
    }

    /**
     * @return The currently playing file's creator
     */
    public ContentCreator getFileOwner() {
        return UserDatabase.getInstance().getCreatorByName(audioFile.getFileOwner(fileState));
    }

    /**
     * Resets the given state
     *
     * @param state the state
     */
    public static void setDefaultState(final AudioFileState state) {
        state.clear();
        state.setOver(false);
        state.setRepeatModeAsString(MediaPlayerConstants.NO_REPEAT_MESSAGE);
    }

    /**
     * Clears player data
     *
     * @param timestamp the timestamp
     * @param stopAd    if the ad should be canceled
     */
    public void clearPlayer(final int timestamp, final boolean stopAd) {
        updateStatus(timestamp);

        if (audioFile != null && !fileState.isOver() && !podcastStates.contains(fileState)) {
            audioFile.removeTie();
        }

        audioFile = null;
        isPlaying = false;

        fileState = new AudioFileState();
        setDefaultState(fileState);
        fileState.setOver(true);

        if (stopAd) {
            adTime = 0;
            adPrice = 0;
        }
    }

    /**
     * Updates the remaining ad time and pays for it if it ends.
     *
     * @param timeForAd the time allocated for the ad
     * @return The time used for the ad
     */
    private int updateAd(final int timeForAd) {
        if (adPrice == 0) {
            return 0;
        }

        if (adTime > timeForAd) {
            adTime -= timeForAd;
            return timeForAd;
        }

        int usedTime = adTime;
        adTime = 0;
        adPrice = 0;

        return usedTime;
    }

    /**
     * Checks if the ad can be played
     *
     * @param elapsedTime the elapsed time
     * @param timeDiff    the remaining time
     * @return The updated elapsed time
     */
    private int checkForRevenue(final int elapsedTime, final int timeDiff) {
        if (adTime == 0 || podcastStates.contains(fileState)) {
            return elapsedTime;
        }

        int surplus = elapsedTime;
        if (elapsedTime >= timeDiff && adTime == MediaPlayerConstants.AD_TIME_START) {
            adTime--;
            playerOwner.addAdRevenue(adPrice);
            surplus = elapsedTime - timeDiff;
        }

        return elapsedTime - ((adTime != MediaPlayerConstants.AD_TIME_START)
                ? updateAd(surplus) : 0);
    }

    /**
     * Simulates the passage of time and updates the status of the player
     *
     * @param timestamp current time
     */
    public void updateStatus(final int timestamp) {
        int elapsedTime = timestamp - lastUpdateTime;
        if (elapsedTime == 0) {
            return;
        }

        lastUpdateTime = timestamp;

        if (!isPlaying || isOffline) {
            return;
        }

        elapsedTime = checkForRevenue(elapsedTime, fileState.getTimeRemaining());
        audioFile.simulateTime(fileState, elapsedTime, playerOwner);

        if (fileState.isOver()) {
            isPlaying = false;
            podcastStates.remove(fileState);
            audioFile.removeTie();

            adTime = 0;
            if (adPrice > 0) {
                playerOwner.addAdRevenue(adPrice);
                adPrice = 0;
            }
        }
    }

    /**
     * Plays a given ad
     *
     * @param timestamp the timestamp
     * @param price     the ad price
     * @return Success status message
     */
    public String insertAd(final int timestamp, final double price) {
        updateStatus(timestamp);

        if (!isPlaying) {
            return playerOwner.getName() + MediaPlayerConstants.PLAYER_NOT_PLAYING;
        }

        adTime = MediaPlayerConstants.AD_TIME_START;
        adPrice = price;

        return MediaPlayerConstants.AD_INSERTED;
    }

    /**
     * Updates the file status and switches the offline status
     *
     * @param status    the new status
     * @param timestamp the timestamp
     */
    public void setOffline(final boolean status, final int timestamp) {
        updateStatus(timestamp);
        isOffline = status;
    }

    /**
     * @param seed      the seed used for shuffling
     * @param timestamp the timestamp
     * @return A message corresponding to the success status
     */
    public String shuffleLoadedFile(final Integer seed, final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return UserConstants.SHUFFLE_NO_SOURCE;
        }

        List<Integer> indexes = audioFile.shuffleFile(seed);
        if ((indexes == null && seed != null) || (seed == null && !fileState.isShuffled())) {
            return UserConstants.SHUFFLE_NOT_VALID;
        }

        PlaylistState playlistState = (PlaylistState) fileState;
        playlistState.setShuffleIndexes(indexes);
        playlistState.setShuffled(!playlistState.isShuffled());

        return (playlistState.isShuffled())
                ? PlaylistConstants.SHUFFLE_ACTIVATE : PlaylistConstants.SHUFFLE_DEACTIVATE;
    }

    /**
     * @param timestamp the timestamp
     * @return A message corresponding to the success status
     */
    public String playNext(final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return MediaPlayerConstants.NEXT_NO_SOURCE;
        }

        String nextName = audioFile.getNext(fileState, playerOwner);
        if (nextName == null) {
            return MediaPlayerConstants.NEXT_NO_SOURCE;
        }

        isPlaying = !fileState.isOver();
        return MediaPlayerConstants.NEXT_SUCCESS + nextName + ".";
    }

    /**
     * @param timestamp the timestamp
     * @return Same as for playNext
     */
    public String playPrevious(final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return MediaPlayerConstants.PREV_NO_SOURCE;
        }

        String message = MediaPlayerConstants.PREV_SUCCESS
                + audioFile.getPrevious(fileState, playerOwner) + ".";
        isPlaying = !fileState.isOver();

        return message;
    }

    /**
     * Adds the current file in the playlist with the given id
     *
     * @param user       the user
     * @param timestamp  the timestamp
     * @param playlistId the playlist id
     * @return Success status message
     */
    public String addRemoveInPlaylist(final User user, final int timestamp, final int playlistId) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return PlaylistConstants.ADD_REMOVE_NO_SOURCE;
        }
        if (!audioFile.canAddToCollection()) {
            return PlaylistConstants.ADD_REMOVE_NOT_SONG;
        }

        return user.addRemoveInPlaylist(audioFile.getCurrentFile(fileState), playlistId);
    }

    /**
     * Skips forward or backward 90 seconds
     *
     * @param type      type of skip
     * @param timestamp current time
     * @return The response message
     */
    public String executeSkip(final String type, final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return type.equals("forward")
                    ? PodcastConstants.SKIP_NO_SOURCE : PodcastConstants.REWIND_NO_SOURCE;
        }

        String message;
        if (type.equals("forward")) {
            message = audioFile.skipForward(fileState, playerOwner);
            isPlaying = !fileState.isOver();
        } else {
            message = audioFile.skipBackward(fileState);
            isPlaying = true;
        }

        return message;
    }

    /**
     * Loads the selected file
     *
     * @param selection the selected file
     * @param syncState notifies if a bookmark should be added
     * @param timestamp the timestamp
     * @return Success status message
     */
    public String loadFile(final AudioFile selection,
                           final boolean syncState,
                           final int timestamp) {
        if (selection == null) {
            return MediaPlayerConstants.LOAD_NO_SOURCE;
        }
        if (selection.isEmpty()) {
            return MediaPlayerConstants.LOAD_EMPTY_COLLECTION;
        }
        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }

        clearPlayer(timestamp, true);

        audioFile = selection;
        fileState = null;

        if (syncState) {
            fileState = podcastStates.stream()
                    .filter(state -> state.getPodcastName().equals(selection.getName()))
                    .findAny().orElse(null);
        }

        if (fileState == null) {
            fileState = selection.getInitialState();
            if (syncState) {
                podcastStates.add((PodcastState) fileState);
            }
        }

        if (syncState) {
            playerOwner.listenTo((Podcast) selection, fileState.getLastIndex(), 1);
        } else {
            playerOwner.listenTo((Song) selection.getCurrentFile(fileState), 1);
        }

        isPlaying = !fileState.isOver();
        audioFile.addTie();

        return MediaPlayerConstants.LOAD_SUCCESS;
    }

    /**
     * Turns the player on/off
     *
     * @param timestamp the timestamp
     * @return Success status message
     */
    public String playPauseFile(final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return MediaPlayerConstants.PLAY_PAUSE_NO_SOURCE;
        }

        String message = (isPlaying)
                ? MediaPlayerConstants.MEDIA_PLAYER_PAUSE_SUCCESS
                : MediaPlayerConstants.MEDIA_PLAYER_PLAY_SUCCESS;

        isPlaying = !isPlaying;
        return message;
    }

    /**
     * @param command the input command
     * @return The response to the command
     */
    public MediaPlayerStats getPlayerStatus(final Command command) {
        updateStatus(command.getTimestamp());

        return new MediaPlayerStats(
                (!fileState.isOver()) ? audioFile.getCurrentName(fileState) : "",
                (!fileState.isOver()) ? fileState.getTimeRemaining() : 0,
                fileState.getRepeatModeAsString(),
                !fileState.isOver() && fileState.isShuffled(),
                !isPlaying
        );
    }

    /**
     * Likes the current file
     *
     * @param user      the user
     * @param timestamp the timestamp
     * @return Success status message
     */
    public String likeSong(final User user, final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return UserConstants.LIKE_NO_FILE;
        }
        return user.likeSong(audioFile.getCurrentFile(fileState));
    }

    /**
     * Used for changing the repeat mode
     */
    private void changeRepeatMode() {
        fileState.setRepeatMode(
                (fileState.getRepeatMode() + 1) % MediaPlayerConstants.REPEAT_MODES
        );
    }

    /**
     * Changes the repeat mode
     *
     * @param timestamp the timestamp
     * @return Success status message
     */
    public String repeatFile(final int timestamp) {
        updateStatus(timestamp);

        if (isOffline) {
            return Admin.userOffline(playerOwner.getName());
        }
        if (fileState.isOver()) {
            return MediaPlayerConstants.REPEAT_NO_SOURCE;
        }

        changeRepeatMode();

        String mode = audioFile.getRepeatModeAsString(fileState.getRepeatMode());
        fileState.setRepeatModeAsString(mode);

        return MediaPlayerConstants.REPEAT_CHANGE + mode.toLowerCase() + ".";
    }
}
