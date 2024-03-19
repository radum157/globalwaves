package databases;

import app.audiofiles.Song;
import app.audiofiles.collections.Playlist;
import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.ContentCreator;
import app.users.creators.Host;
import app.users.tie.TiedUserEntity;
import app.utils.constants.searchbar.SearchBarConstants;
import fileio.commands.search.SearchCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User database class. Implements user searches
 */
@Getter
public final class UserDatabase {
    /* Thread-safe singleton */
    @Getter
    private static UserDatabase instance;

    static {
        synchronized (UserDatabase.class) {
            instance = new UserDatabase();
        }
    }

    private final List<User> users = new ArrayList<>();
    private final List<Artist> artists = new ArrayList<>();
    private final List<Host> hosts = new ArrayList<>();

    private UserDatabase() {
    }

    /**
     * Clears all users from the database
     * <p>
     * !!! Method should be removed in production !!!
     */
    public void clear() {
        users.clear();
        artists.clear();
        hosts.clear();
    }

    /**
     * @param username the username
     * @return The creator or null
     */
    public ContentCreator getCreatorByName(final String username) {
        Artist artist = getArtistByName(username);
        if (artist != null) {
            return artist;
        }
        return getHostByName(username);
    }

    /**
     * Updates the file state of all media players
     *
     * @param timestamp the timestamp
     */
    public void updateAllPlayers(final int timestamp) {
        for (User user : users) {
            user.getMediaPlayer().updateStatus(timestamp);
        }
    }

    /**
     * Removes the given songs from all users' favorites
     *
     * @param songs the songs
     */
    public void removeSongs(final List<Song> songs) {
        for (User user : users) {
            user.getLikedSongs().removeAll(songs);
        }
    }

    /**
     * Removes the given playlists from all users' followed playlists list
     *
     * @param playlists the playlists
     */
    public void removePlaylists(final List<Playlist> playlists) {
        for (User user : users) {
            user.getFollowedPlaylists().removeAll(playlists);
        }
    }

    /**
     * Searches for any type of user by name
     *
     * @param username the username
     * @return The user entity with the given username
     */
    public TiedUserEntity findUsername(final String username) {
        TiedUserEntity foundUser = getUserByName(username);
        if (foundUser == null) {
            foundUser = getArtistByName(username);
        }
        return (foundUser != null) ? foundUser : getHostByName(username);
    }

    /**
     * @param username the username
     * @return If the username is already taken
     */
    public boolean containsUsername(final String username) {
        return findUsername(username) != null;
    }

    /**
     * Removes a given user entity of any type
     *
     * @param user the user
     * @return Success status message
     */
    public String removeUser(final TiedUserEntity user) {
        if (user.getTies() > 0) {
            return "can't be deleted.";
        }

        user.untieFrom(Library.getInstance());

        users.remove(user);
        artists.remove(user);
        hosts.remove(user);

        return "was successfully deleted.";
    }

    /**
     * @return A list of all usernames. The order is normal, artist, host
     */
    public List<String> getAllUsers() {
        List<String> result = users.stream().map(User::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        result.addAll(artists.stream().map(Artist::getName).toList());
        result.addAll(hosts.stream().map(Host::getName).toList());

        return result;
    }

    /**
     * @return A list of all online users
     */
    public List<String> getOnlineUsers() {
        return users.stream().filter(User::isConnected)
                .map(User::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @return The top 5 artists (by total album likes)
     */
    public List<String> getTop5Artists() {
        List<Artist> sortedArtists = new ArrayList<>(artists);
        sortedArtists.sort(Comparator.comparing(Artist::getLikes).reversed());

        return sortedArtists.subList(
                0, Math.min(artists.size(), SearchBarConstants.RESULTS_MAX_SIZE)
        ).stream().map(Artist::getName).toList();
    }

    /**
     * @param filters search filters
     * @return The found artists
     */
    public List<ContentCreator> getArtistsByFilters(final SearchCommand.SearchFilter filters) {
        List<ContentCreator> results = new ArrayList<>(artists);

        if (filters.getName() != null) {
            results.removeIf(artist -> !artist.getName().startsWith(filters.getName()));
        }

        return results;
    }

    /**
     * @param filters search filters
     * @return The found hosts
     */
    public List<ContentCreator> getHostsByFilters(final SearchCommand.SearchFilter filters) {
        List<ContentCreator> results = new ArrayList<>(hosts);

        if (filters.getName() != null) {
            results.removeIf(artist -> !artist.getName().startsWith(filters.getName()));
        }

        return results;
    }

    /**
     * @param username the user's name
     * @return The user or null
     */
    public User getUserByName(final String username) {
        return users.stream().filter(user -> user.getName().equals(username))
                .findAny()
                .orElse(null);
    }

    /**
     * @param username the artist's username
     * @return The artist or null
     */
    public Artist getArtistByName(final String username) {
        return artists.stream().filter(artist -> artist.getName().equals(username))
                .findAny()
                .orElse(null);
    }

    /**
     * @param username the host's username
     * @return The host or null
     */
    public Host getHostByName(final String username) {
        return hosts.stream().filter(host -> host.getName().equals(username))
                .findAny()
                .orElse(null);
    }

    /**
     * @param user playlist to be added
     */
    public void addUser(final User user) {
        users.add(user);
    }

    /**
     * @param artist playlist to be added
     */
    public void addArtist(final Artist artist) {
        artists.add(artist);
    }

    /**
     * @param host playlist to be added
     */
    public void addHost(final Host host) {
        hosts.add(host);
    }
}
