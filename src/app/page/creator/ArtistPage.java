package app.page.creator;

import app.audiofiles.collections.Album;
import app.page.Page;
import app.utils.constants.page.PageConstants;
import app.utils.page.PageInfo;
import app.utils.page.data.Event;
import app.utils.page.data.Merchandise;

import java.util.ArrayList;
import java.util.List;

public final class ArtistPage extends Page {
    private final List<Album> albums;
    private final List<Merchandise> merch = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();

    public ArtistPage(final PageInfo pageInfo, final List<Album> albums) {
        super(pageInfo);
        this.albums = albums;
    }

    /**
     * @param merchName the name of the merchandise
     * @return The merch or null
     */
    public Merchandise getMerchByName(final String merchName) {
        return merch.stream().filter(merchandise -> merchandise.name().equals(merchName))
                .findAny().orElse(null);
    }

    /**
     * Searches for merch by name
     *
     * @param merchName the name
     * @return If any merch with the given name exists
     */
    public boolean containsMerch(final String merchName) {
        return getMerchByName(merchName) != null;
    }

    /**
     * Searches for an event by name
     *
     * @param eventName the name
     * @return If any was found
     */
    public boolean containsEvent(final String eventName) {
        return events.stream().filter(event -> event.name().equals(eventName))
                .findAny().orElse(null) != null;
    }

    /**
     * Adds new merch
     *
     * @param merchandise the merchandise
     */
    public void addMerch(final Merchandise merchandise) {
        merch.add(merchandise);
    }

    /**
     * Adds a new event
     *
     * @param event the event
     */
    public void addEvent(final Event event) {
        events.add(event);
    }

    /**
     * @param eventName the event to be removed
     * @return If any was found
     */
    public String removeEvent(final String eventName) {
        if (events.removeIf(event -> event.name().equals(eventName))) {
            return PageConstants.EVENT_DELETED;
        }
        return PageConstants.NO_EVENT;
    }

    @Override
    public String printPage() {
        StringBuilder content = new StringBuilder("Albums:\n\t[");

        for (Album album : albums) {
            content.append(album.getName()).append(", ");
        }

        if (!albums.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]\n\nMerch:\n\t[");

        for (Merchandise item : merch) {
            content.append(item.name()).append(" - ")
                    .append(item.price()).append(":\n\t")
                    .append(item.description()).append(", ");
        }

        if (!merch.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]\n\nEvents:\n\t[");

        for (Event event : events) {
            content.append(event.name()).append(" - ")
                    .append(event.date().format(PageConstants.EVENT_DATE_FORMAT)).append(":\n\t")
                    .append(event.description()).append(", ");
        }

        if (!events.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]");

        return content.toString();
    }
}
