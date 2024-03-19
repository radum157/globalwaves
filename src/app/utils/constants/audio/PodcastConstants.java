package app.utils.constants.audio;

/**
 * Constants used in podcast-related operations
 */
public final class PodcastConstants {
    private PodcastConstants() {
    }

    public static final int SKIP_SECOND_COUNT = 90;
    public static final String SKIP_MESSAGE = "Skipped forward successfully.";
    public static final String REWIND_MESSAGE = "Rewound successfully.";
    public static final String SKIP_NOT_PODCAST = "The loaded source is not a podcast.";
    public static final String SKIP_NO_SOURCE =
            "Please load a source before attempting to forward.";
    public static final String REWIND_NO_SOURCE = "Please select a source before rewinding.";
}
