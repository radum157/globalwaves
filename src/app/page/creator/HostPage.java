package app.page.creator;

import app.audiofiles.collections.Podcast;
import app.page.Page;
import app.utils.constants.page.PageConstants;
import app.utils.page.PageInfo;
import app.utils.page.data.Announcement;
import fileio.input.EpisodeInput;

import java.util.ArrayList;
import java.util.List;

public final class HostPage extends Page {
    private final List<Podcast> podcasts;
    private final List<Announcement> announcements = new ArrayList<>();

    public HostPage(final PageInfo pageInfo, final List<Podcast> podcasts) {
        super(pageInfo);
        this.podcasts = podcasts;
    }

    /**
     * @param announcementName the name of the announcement
     * @return If an announcement with the same name already exists
     */
    public boolean containsAnnouncement(final String announcementName) {
        return announcements.stream()
                .filter(announcement -> announcement.name().equals(announcementName))
                .findAny().orElse(null) != null;
    }

    /**
     * Adds a new announcement
     *
     * @param announcement the announcement
     */
    public void addAnnouncement(final Announcement announcement) {
        announcements.add(announcement);
    }

    /**
     * Removes an announcement
     *
     * @param announcementName the announcement name
     * @return Success status message
     */
    public String removeAnnouncement(final String announcementName) {
        if (announcements.removeIf(announcement -> announcement.name().equals(announcementName))) {
            return PageConstants.ANNOUNCE_DELETED;
        }
        return PageConstants.NO_ANNOUNCEMENT;
    }

    @Override
    public String printPage() {
        StringBuilder content = new StringBuilder("Podcasts:\n\t[");

        for (Podcast podcast : podcasts) {
            content.append(podcast.getName()).append(":\n\t[");
            for (EpisodeInput episode : podcast.getEpisodes()) {
                content.append(episode.getName()).append(" - ")
                        .append(episode.getDescription()).append(", ");
            }
            content.setLength(content.length() - 2);
            content.append("]\n, ");
        }

        if (!podcasts.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]\n\nAnnouncements:\n\t[");

        for (Announcement announcement : announcements) {
            content.append(announcement.name()).append(":\n\t")
                    .append(announcement.description()).append("\n, ");
        }

        if (!announcements.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]");

        return content.toString();
    }
}
