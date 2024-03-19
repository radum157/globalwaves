package app.utils.constants.page;

import java.time.format.DateTimeFormatter;

public final class PageConstants {
    private PageConstants() {
    }

    public static final String NO_PAGE = " is trying to access a non-existing page.";
    public static final DateTimeFormatter EVENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final String EVENT_DELETED = "deleted the event successfully.";
    public static final String NO_EVENT = "doesn't have an event with the given name.";
    public static final String ANNOUNCE_DELETED = "successfully deleted the announcement.";
    public static final String NO_ANNOUNCEMENT = "no announcement with the given name.";
    public static final String BUY_PAGE_INVALID = "Cannot buy merch from this page.";
    public static final String MERCH_BUY_SUCCESS = " has added new merch successfully.";
    public static final String SUBSCRIBE_FAIL =
            "To subscribe you need to be on the page of an artist or host.";
    public static final String BROWSE_FAIL = "There are no pages left to go ";
}
