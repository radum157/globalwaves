package app.admin;

import app.users.creators.ContentCreator;
import app.utils.constants.page.PageConstants;
import factory.tie.TieFactory;
import app.utils.constants.users.ArtistConstants;
import app.utils.constants.users.HostConstants;
import app.utils.constants.users.UserConstants;
import databases.UserDatabase;
import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;
import app.users.tie.TiedUserEntity;
import fileio.input.LibraryInput;
import app.audiofiles.collections.Podcast;
import app.audiofiles.Song;
import fileio.commands.wrapper.CommandWrapper;
import databases.Library;

/**
 * Utility class for admin operations
 */
public final class Admin {
    private static Library library = Library.getInstance();
    private static UserDatabase userDatabase = UserDatabase.getInstance();

    /**
     * String builder utility method
     *
     * @param username the username
     * @return A string indicating the given username does not exist
     */
    public static String noUserFound(final String username) {
        return "The username " + username + " doesn't exist.";
    }

    /**
     * String builder utility method
     *
     * @param username the username
     * @return A string indicating the given user is offline
     */
    public static String userOffline(final String username) {
        return username + " is offline.";
    }

    /**
     * Updates the recommendation lists of a given user
     *
     * @param input the parameters
     * @return Sucess status message
     */
    public String updateRecommendations(final CommandWrapper input) {
        if (!userDatabase.containsUsername(input.getUsername())) {
            return noUserFound(input.getUsername());
        }

        User user = userDatabase.getUserByName(input.getUsername());
        if (user == null) {
            return input.getUsername() + UserConstants.USER_NOT_NORMAL;
        }

        user.getMediaPlayer().updateStatus(input.getTimestamp());

        return switch (input.getRecommendationType()) {
            case "random_song" -> user.updateRandomSong(library);
            case "random_playlist" -> user.updateRandomPlaylist(library);
            case "fans_playlist" -> user.updateFansPlaylist(
                    input.getTimestamp(), userDatabase
            );
            default -> null;
        };
    }

    /**
     * Adds a new subscription
     *
     * @param user  the user
     * @param input the parameters
     * @return Success status message
     */
    public String addSubscription(final User user, final CommandWrapper input) {
        if (user == null) {
            return noUserFound(input.getUsername());
        }

        ContentCreator creator = user.getCurrentPage().getPageInfo().getCreator();
        if (creator == null) {
            return PageConstants.SUBSCRIBE_FAIL;
        }

        return user.getName() + ((creator.addObserver(user))
                ? " subscribed to " : " unsubscribed from ")
                + creator.getName() + " successfully.";
    }

    /**
     * Handles a merch buy transaction
     *
     * @param user  the user
     * @param input the parameters
     * @return Success status message
     */
    public String doBuyMerch(final User user, final CommandWrapper input) {
        if (user == null) {
            return noUserFound(input.getUsername());
        }
        return user.buyMerch(input.getName());
    }

    /**
     * Sends an ad to a user's media player
     *
     * @param user  the user
     * @param input the parameters
     * @return Success status message
     */
    public String insertAd(final User user, final CommandWrapper input) {
        if (user == null) {
            return noUserFound(input.getUsername());
        }
        return user.getMediaPlayer().insertAd(input.getTimestamp(), input.getPrice());
    }

    /**
     * Switches the connection status of a given user
     *
     * @param username the username
     * @return Success status message
     */
    public String switchConnectionOf(final String username, final int timestamp) {
        if (!userDatabase.containsUsername(username)) {
            return noUserFound(username);
        }

        User user = userDatabase.getUserByName(username);
        if (user == null) {
            return username + UserConstants.USER_NOT_NORMAL;
        }

        user.switchConnectionStatus(timestamp);
        return username + UserConstants.CONNECTION_SWITCH;
    }

    /**
     * Adds a new user
     *
     * @param command the parameters
     * @return The success status message
     */
    public String addUser(final CommandWrapper command) {
        String message = "The username " + command.getUsername();

        if (userDatabase.containsUsername(command.getUsername())) {
            return message + UserConstants.USERNAME_TAKEN;
        }

        TieFactory tieFactory = new TieFactory();
        if (command.getType().equals("user")) {
            userDatabase.addUser(tieFactory.createUser(command));
        } else if (command.getType().equals("artist")) {
            userDatabase.addArtist(tieFactory.createArtist(command));
        } else {
            Host host = tieFactory.createHost(command);
            userDatabase.addHost(host);

            /* Check for existing podcasts */
            library.getPodcasts().forEach(podcast -> {
                if (podcast.getOwner().equals(host.getName())) {
                    host.getPodcasts().add(podcast);
                }
            });
        }

        return message + UserConstants.ADD_SUCCESS;
    }

    /**
     * Deletes a user
     *
     * @param command the parameters
     * @return The success status message
     */
    public String deleteUser(final CommandWrapper command) {
        TiedUserEntity userEntity = userDatabase.findUsername(command.getUsername());
        if (userEntity == null) {
            return noUserFound(command.getUsername());
        }

        userDatabase.updateAllPlayers(command.getTimestamp());
        return userEntity.getName() + " " + userDatabase.removeUser(userEntity);
    }

    /**
     * Adds a podcast for a host
     *
     * @param command the parameters
     * @return Success status message
     */
    public String addPodcast(final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return noUserFound(command.getUsername());
        }

        Host host = userDatabase.getHostByName(command.getUsername());
        if (host == null) {
            return command.getUsername() + HostConstants.USER_NOT_HOST;
        }

        if (host.findPodcast(command.getName()) != null) {
            return host.getName() + HostConstants.DUPLICATE_PODCAST;
        }

        TieFactory tieFactory = new TieFactory();
        return host.addPodcast(tieFactory.createPodcast(command));
    }

    /**
     * Removes an existing podcast
     *
     * @param command the parameters
     * @return Success status message
     */
    public String removePodcast(final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return noUserFound(command.getUsername());
        }

        Host host = userDatabase.getHostByName(command.getUsername());
        if (host == null) {
            return command.getUsername() + HostConstants.USER_NOT_HOST;
        }

        return host.removePodcast(command.getName());
    }

    /**
     * Adds a new album for an artist
     *
     * @param command the parameters
     * @return Success status message
     */
    public String addAlbum(final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return noUserFound(command.getUsername());
        }

        Artist artist = userDatabase.getArtistByName(command.getUsername());
        if (artist == null) {
            return command.getUsername() + ArtistConstants.USER_NOT_ARTIST;
        }

        if (artist.findAlbum(command.getName()) != null) {
            return artist.getName() + ArtistConstants.DUPLICATE_ALBUM;
        }

        TieFactory tieFactory = new TieFactory();
        return artist.addAlbum(tieFactory.createAlbum(command));
    }

    /**
     * Removes an album
     *
     * @param command the parameters
     * @return Success status message
     */
    public String removeAlbum(final CommandWrapper command) {
        if (!userDatabase.containsUsername(command.getUsername())) {
            return noUserFound(command.getUsername());
        }

        Artist artist = userDatabase.getArtistByName(command.getUsername());
        if (artist == null) {
            return command.getUsername() + ArtistConstants.USER_NOT_ARTIST;
        }

        return artist.removeAlbum(command.getName());
    }

    /**
     * Switches the premium status of a given user to the given status
     *
     * @param user  the user
     * @param input the parameters
     * @return Success status message
     */
    public String switchPremium(final User user, final CommandWrapper input) {
        if (user == null) {
            return noUserFound(input.getUsername());
        }

        if (input.getCommand().equals("buyPremium") && user.isPremium()) {
            return user.getName() + UserConstants.ALREADY_PREMIUM;
        }
        if (input.getCommand().equals("cancelPremium") && !user.isPremium()) {
            return user.getName() + UserConstants.NOT_PREMIUM;
        }

        user.getMediaPlayer().updateStatus(input.getTimestamp());
        return user.switchPremium();
    }

    /**
     * Adds all data stored in a LibraryInput to the databases
     *
     * @param libraryInput the library input
     */
    public static void addToDatabase(final LibraryInput libraryInput) {
        libraryInput.getSongs().forEach(songInput -> library.addSong(new Song(songInput)));
        libraryInput.getPodcasts().forEach(podcastInput ->
                library.addPodcast(new Podcast(podcastInput))
        );
        libraryInput.getUsers().forEach(userInput -> userDatabase.addUser(new User(userInput)));
    }
}
