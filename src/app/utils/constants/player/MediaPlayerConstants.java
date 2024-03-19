package app.utils.constants.player;

/**
 * Useful constants for media player operations
 */
public final class MediaPlayerConstants {
    private MediaPlayerConstants() {
    }

    public static final Integer REPEAT_MODES = 3;
    public static final String PLAYER_NOT_PLAYING = " is not playing any music.";
    public static final String AD_INSERTED = "Ad inserted successfully.";
    public static final int AD_TIME_START = 11;
    public static final String NO_SOURCE = "Please select a source before ";
    public static final String NO_LOADED = "Please load a source before ";
    public static final String LOAD_NO_SOURCE = NO_SOURCE + "attempting to load.";
    public static final String LOAD_SUCCESS = "Playback loaded successfully.";
    public static final String LOAD_EMPTY_COLLECTION = "You can't load an empty audio collection!";
    public static final String PLAY_PAUSE_NO_SOURCE =
            "Please load a source before attempting to pause or resume playback.";
    public static final String MEDIA_PLAYER_PAUSE_SUCCESS = "Playback paused successfully.";
    public static final String MEDIA_PLAYER_PLAY_SUCCESS = "Playback resumed successfully.";
    public static final String REPEAT_CHANGE = "Repeat mode changed to ";
    public static final String NO_REPEAT_MESSAGE = "No Repeat";
    public static final String REPEAT_ALL_MESSAGE = "Repeat All";
    public static final String REPEAT_CURRENT_MESSAGE = "Repeat Current Song";
    public static final String REPEAT_INFINITE_MESSAGE = "Repeat Infinite";
    public static final String REPEAT_ONCE_MESSAGE = "Repeat Once";
    public static final String REPEAT_NO_SOURCE = NO_LOADED + "setting the repeat status.";
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_ALL_ONCE = 1;
    public static final int REPEAT_CURRENT_INFINITE = 2;
    public static final String NEXT_NO_SOURCE = NO_LOADED + "skipping to the next track.";
    public static final String PREV_NO_SOURCE = NO_LOADED + "returning to the previous track.";
    public static final String NEXT_SUCCESS =
            "Skipped to next track successfully. The current track is ";
    public static final String PREV_SUCCESS =
            "Returned to previous track successfully. The current track is ";
}
