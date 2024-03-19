package app.utils.constants.audio;

/**
 * Useful constants for playlist-related operations
 */
public final class PlaylistConstants {
    private PlaylistConstants() {
    }

    public static final String SHUFFLE_ACTIVATE = "Shuffle function activated successfully.";
    public static final String SHUFFLE_DEACTIVATE = "Shuffle function deactivated successfully.";
    public static final String ADD_REMOVE_NO_SOURCE =
            "Please load a source before adding to or removing from the playlist.";
    public static final String ADD_REMOVE_NO_PLAYLIST = "The specified playlist does not exist.";
    public static final String ADD_REMOVE_NOT_SONG = "The loaded source is not a song.";
    public static final String ADD_SUCCESS = "Successfully added to playlist.";
    public static final String REMOVE_SUCCESS = "Successfully removed from playlist.";
    public static final String PLAYLIST_EXISTS = "A playlist with the same name already exists.";
    public static final String CREATE_SUCCESS = "Playlist created successfully.";
    public static final String UPDATE_PRIVACY = "Visibility status updated successfully to ";
    public static final String FOLLOW_NO_SOURCE =
            "Please select a source before following or unfollowing.";
    public static final String FOLLOW_NOT_PLAYLIST = "The selected source is not a playlist.";
    public static final String FOLLOW_SUCCESS = "Playlist followed successfully.";
    public static final String UNFOLLOW_SUCCESS = "Playlist unfollowed successfully.";
    public static final String FOLLOW_OWN_ERROR =
            "You cannot follow or unfollow your own playlist.";
}
