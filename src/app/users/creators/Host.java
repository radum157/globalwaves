package app.users.creators;

import app.utils.constants.users.HostConstants;
import databases.Library;
import app.page.Page;
import app.utils.page.PageInfo;
import fileio.input.EpisodeInput;
import fileio.input.UserInput;
import app.audiofiles.collections.Podcast;
import app.page.creator.HostPage;
import lombok.Getter;
import visitor.UserVisitor;
import visitor.VisitableUser;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class Host extends ContentCreator implements VisitableUser {
    private final HostPage hostPage;
    private final List<Podcast> podcasts = new ArrayList<>();

    public Host(final UserInput userInput) {
        super(userInput);
        hostPage = new HostPage(new PageInfo(this, PageInfo.PageType.HOST), podcasts);
    }

    /**
     * @param episode the episode (reference)
     * @return If the host hosts the given episode
     */
    public boolean hostsEpisode(final EpisodeInput episode) {
        return podcasts.stream().filter(podcast -> podcast.getEpisodes().contains(episode))
                .findAny().orElse(null) != null;
    }

    @Override
    public void accept(final UserVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void untieFrom(final Library library) {
        library.removePodcasts(podcasts);
    }

    /**
     * @param podcastName the podcast name
     * @return The found podcast or null
     */
    public Podcast findPodcast(final String podcastName) {
        return podcasts.stream().filter(podcast -> podcast.getName().equals(podcastName))
                .findAny().orElse(null);
    }

    /**
     * Adds a new podcast
     *
     * @param podcast the podcast
     * @return Success status message
     */
    public String addPodcast(final Podcast podcast) {
        if (podcast.getEpisodes().stream().anyMatch(episode ->
                podcast.getEpisodes().stream().anyMatch(episode2 -> !episode2.equals(episode)
                        && episode2.getName().equals(episode.getName())))) {
            return getName() + HostConstants.DUPLICATE_EPISODE;
        }

        Library.getInstance().addPodcast(podcast);
        podcasts.add(podcast);
        return getName() + HostConstants.ADD_SUCCESS;
    }

    /**
     * Removes a podcast
     *
     * @param podcastName the podcast name
     * @return Success status message
     */
    public String removePodcast(final String podcastName) {
        Podcast podcast = findPodcast(podcastName);
        if (podcast == null) {
            return getName() + HostConstants.NO_PODCAST;
        }

        if (podcast.getTies() > 0) {
            return getName() + HostConstants.DELETE_FAIL;
        }

        Library.getInstance().removePodcast(podcast);
        podcasts.remove(podcast);
        return getName() + HostConstants.DELETE_SUCCESS;
    }

    @Override
    public Page getPage() {
        return hostPage;
    }
}
