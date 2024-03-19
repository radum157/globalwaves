package app.page.handler;

import app.page.Page;
import fileio.commands.wrapper.CommandWrapper;
import databases.UserDatabase;
import app.users.normal.User;
import app.admin.Admin;
import app.users.creators.Artist;
import app.users.creators.ContentCreator;
import app.users.creators.Host;
import app.utils.constants.page.PageConstants;
import app.utils.constants.searchbar.SearchBarConstants;
import app.utils.constants.users.ArtistConstants;
import app.utils.page.data.Announcement;
import app.utils.page.data.Event;
import app.utils.page.data.Merchandise;

import java.util.List;

/**
 * Admin-like class for pages
 */
public final class PageHandler {
    /**
     * Browses through the page history
     *
     * @param user  the user
     * @param input the parameters
     * @return Success status message
     */
    public String browsePages(final User user, final CommandWrapper input) {
        boolean result = (input.getCommand().equals("nextPage"))
                ? user.getNextPage() : user.getPreviousPage();

        if (!result) {
            return PageConstants.BROWSE_FAIL + ((input.getCommand().equals("nextPage")
                    ? "forward" : "back")) + ".";
        }
        return "The user " + user.getName() + " has navigated successfully to the "
                + ((input.getCommand().equals("nextPage") ? "next" : "previous"))
                + " page.";
    }

    /**
     * Adds merch to an artist's page
     *
     * @param userDatabase the user database instance
     * @param command      the parameters
     * @return Success status message
     */
    public String addMerchTo(final UserDatabase userDatabase,
                             final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return Admin.noUserFound(command.getUsername());
        }

        Artist artist = userDatabase.getArtistByName(command.getUsername());
        if (artist == null) {
            return command.getUsername() + " is not an artist.";
        }

        if (artist.getArtistPage().containsMerch(command.getName())) {
            return command.getUsername() + " has merchandise with the same name.";
        }

        if (command.getPrice() < 0) {
            return ArtistConstants.MERCH_NEG_PRICE;
        }

        artist.getArtistPage().addMerch(Merchandise.parseFromCommand(command, artist));
        artist.notifyAll("Merchandise");

        return artist.getName() + " has added new merchandise successfully.";
    }

    /**
     * Adds a new event to an artist's page
     *
     * @param userDatabase the user database instance
     * @param command      the parameters
     * @return Success status message
     */
    public String addEventTo(final UserDatabase userDatabase,
                             final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return Admin.noUserFound(command.getUsername());
        }

        Artist artist = userDatabase.getArtistByName(command.getUsername());
        if (artist == null) {
            return command.getUsername() + " is not an artist.";
        }

        if (artist.getArtistPage().containsEvent(command.getName())) {
            return artist.getName() + " has another event with the same name.";
        }

        Event event = Event.parseFromCommand(command);
        if (event == null) {
            return "Event for " + artist.getName() + " does not have a valid date.";
        }

        artist.getArtistPage().addEvent(event);
        artist.notifyAll("Event");

        return artist.getName() + " has added new event successfully.";
    }

    /**
     * Removes an event from an artist's page
     *
     * @param userDatabase the user database instance
     * @param command      the parameters
     * @return Success status message
     */
    public String removeEventFrom(final UserDatabase userDatabase,
                                  final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return Admin.noUserFound(command.getUsername());
        }

        Artist artist = userDatabase.getArtistByName(command.getUsername());
        if (artist == null) {
            return command.getUsername() + " is not an artist.";
        }

        return artist.getName() + " " + artist.getArtistPage().removeEvent(command.getName());
    }

    /**
     * Adds a new announcement to a host's page
     *
     * @param userDatabase the user database instance
     * @param command      the parameters
     * @return Success status message
     */
    public String addAnnouncementTo(final UserDatabase userDatabase,
                                    final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return Admin.noUserFound(command.getUsername());
        }

        Host host = userDatabase.getHostByName(command.getUsername());
        if (host == null) {
            return command.getUsername() + " is not a host.";
        }

        if (host.getHostPage().containsAnnouncement(command.getName())) {
            return host.getName() + " has already added an announcement with this name.";
        }

        host.getHostPage().addAnnouncement(Announcement.parseFromCommand(command));
        host.notifyAll("Announcement");

        return host.getName() + " has successfully added new announcement.";
    }

    /**
     * Removes an announcement from a host's page
     *
     * @param userDatabase the user database instance
     * @param command      the parameters
     * @return Success status message
     */
    public String removeAnnouncementFrom(final UserDatabase userDatabase,
                                         final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return Admin.noUserFound(command.getUsername());
        }

        Host host = userDatabase.getHostByName(command.getUsername());
        if (host == null) {
            return command.getUsername() + " is not a host.";
        }

        return host.getName() + " has " + host.getHostPage().removeAnnouncement(command.getName());
    }

    /**
     * Selects a content creator's page
     *
     * @param creators the creator array
     * @param index    the index
     * @param user     the current user
     * @return Success status message
     */
    public String visitPage(final List<ContentCreator> creators, final int index,
                            final User user) {
        if (index > creators.size()) {
            return SearchBarConstants.SEARCHBAR_HIGH_ID;
        }

        user.getCurrentPage().removeTie();
        user.setCurrentPage(creators.get(index - 1).getPage());
        user.getCurrentPage().addTie();

        return "Successfully selected " + creators.get(index - 1).getName() + "'s page.";
    }

    /**
     * @param user     the user
     * @param pageName the page name
     * @return The next page or null if changePage cannot be applied
     */
    private Page validatePageName(final User user, final String pageName) {
        return switch (pageName) {
            case "Home" -> user.getHomePage();
            case "LikedContent" -> user.getLikedContentPage();
            case "Artist", "Host" -> user.getMediaPlayer().getFileOwner().getPage();
            default -> null;
        };
    }

    /**
     * Changes the page to home or liked content
     *
     * @param user      the user requesting the change
     * @param pageName  the page name
     * @param timestamp the timestamp
     * @return The success status message
     */
    public String changePage(final User user, final String pageName, final int timestamp) {
        if (!user.isConnected()) {
            return user.getName() + " is offline.";
        }

        user.getMediaPlayer().updateStatus(timestamp);

        Page nextPage = validatePageName(user, pageName);
        if (nextPage == null) {
            return user.getName() + PageConstants.NO_PAGE;
        }

        user.setCurrentPage(nextPage);
        return user.getName() + " accessed " + pageName + " successfully.";
    }

    /**
     * @param user the user
     * @return The content of the current page
     */
    public String printPage(final User user) {
        if (!user.isConnected()) {
            return user.getName() + " is offline.";
        }
        return user.getCurrentPage().printPage();
    }
}
