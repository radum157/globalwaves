package app.utils.audiostate;

import lombok.Getter;

/**
 * Podcast "bookmark" class
 */
@Getter
public final class PodcastState extends AudioFileState {
    private final String podcastName;

    public PodcastState(final String podcastName) {
        this.podcastName = podcastName;
    }
}
