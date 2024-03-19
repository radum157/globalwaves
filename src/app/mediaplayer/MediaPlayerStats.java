package app.mediaplayer;

/**
 * Record used for JSON writing of a media player's status.
 */
public record MediaPlayerStats(String name, int remainedTime, String repeat,
                               boolean shuffle, boolean paused) {
}
