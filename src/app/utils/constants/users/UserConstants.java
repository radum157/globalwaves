package app.utils.constants.users;

/**
 * Useful constants for user operations
 */
public final class UserConstants {
    private UserConstants() {
    }

    public static final String LIKE_NO_FILE = "Please load a source before liking or unliking.";
    public static final String LIKE_NOT_SONG = "Loaded source is not a song.";
    public static final String LIKE_SUCCESS = "Like registered successfully.";
    public static final String DISLIKE_SUCCESS = "Unlike registered successfully.";
    public static final String SHUFFLE_NO_SOURCE =
            "Please load a source before using the shuffle function.";
    public static final String SHUFFLE_NOT_VALID =
            "The loaded source is not a playlist or an album.";
    public static final String PLAYLIST_ID_TOO_HIGH = "The specified playlist ID is too high.";
    public static final String USER_NOT_NORMAL = " is not a normal user.";
    public static final String CONNECTION_SWITCH = " has changed status successfully.";
    public static final String USERNAME_TAKEN = " is already taken.";
    public static final String ADD_SUCCESS = " has been added successfully.";
    public static final String PREM_BUY = " bought the subscription successfully.";
    public static final String PREM_CANCEL = " cancelled the subscription successfully.";
    public static final String ALREADY_PREMIUM = " is already a premium user.";
    public static final String NOT_PREMIUM = " is not a premium user.";
    public static final double PREM_COST = 1000000;
    public static final int RECOMMENDATION_MIN_TIME = 30;
    public static final String RECOMMENDATION_FAIL = "No new recommendations were found";
    public static final int RANDOM_PLAYLIST_GENRES = 3;
    public static final int[] GENRE_COUNT = {5, 3, 2};
    public static final String LOAD_FAIL = "No recommendations available.";
}
