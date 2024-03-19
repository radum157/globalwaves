package fileio.commands.utils.formats;

import app.audiofiles.collections.Podcast;
import fileio.input.EpisodeInput;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class PodcastFormat {
    private final String name;
    private final List<String> episodes = new ArrayList<>();

    public PodcastFormat(final Podcast podcast) {
        name = podcast.getName();
        for (EpisodeInput episode : podcast.getEpisodes()) {
            episodes.add(episode.getName());
        }
    }

    /**
     * Formats an arraylist of podcasts
     *
     * @param podcasts the podcast list
     * @return The formatted list
     */
    public static List<PodcastFormat> formatList(final List<Podcast> podcasts) {
        List<PodcastFormat> result = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            result.add(new PodcastFormat(podcast));
        }

        return result;
    }
}
