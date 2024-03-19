package fileio.commands.utils.stats;

import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;
import app.utils.constants.searchbar.SearchBarConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.UserDatabase;
import lombok.Getter;
import visitor.UserVisitor;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class used to store "wrapped" data
 */
public final class StatResult implements UserVisitor {
    private Map<String, Integer> topArtists;
    private Map<String, Integer> topGenres;
    private Map<String, Integer> topSongs;
    private Map<String, Integer> topAlbums;
    private Map<String, Integer> topEpisodes;
    private List<String> topFans;
    private Integer listeners;

    @Getter
    private boolean empty;

    /**
     * Adds all non-null properties to a given object node
     *
     * @param userType the user type (to add only specific results)
     */
    public ObjectNode toNode(final String userType) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        if (userType.equals("user")) {
            objectNode.putPOJO("topArtists", topArtists);
            objectNode.putPOJO("topGenres", topGenres);
            objectNode.putPOJO("topSongs", topSongs);
            objectNode.putPOJO("topAlbums", topAlbums);
            objectNode.putPOJO("topEpisodes", topEpisodes);
        } else if (userType.equals("artist")) {
            objectNode.putPOJO("topAlbums", topAlbums);
            objectNode.putPOJO("topSongs", topSongs);
            objectNode.putPOJO("topFans", topFans);
            objectNode.put("listeners", listeners);
        } else {
            objectNode.putPOJO("topEpisodes", topEpisodes);
            objectNode.put("listeners", listeners);
        }

        return objectNode;
    }

    /**
     * Initialise variables for user stats
     */
    private void prepareForUser() {
        topArtists = new LinkedHashMap<>();
        topGenres = new LinkedHashMap<>();
        topSongs = new LinkedHashMap<>();
        topAlbums = new LinkedHashMap<>();
        topEpisodes = new LinkedHashMap<>();
    }

    /**
     * Initialise variables for artist stats
     */
    private void prepareForArtist() {
        topAlbums = new LinkedHashMap<>();
        topSongs = new LinkedHashMap<>();
        topFans = new ArrayList<>();
        listeners = 0;
    }

    /**
     * Initialise variables for host stats
     */
    private void prepareForHost() {
        topEpisodes = new LinkedHashMap<>();
        listeners = 0;
    }

    /**
     * Sort a map by its values. Truncates the number of entries
     *
     * @param map the map
     * @return The sorted map
     */
    private Map<String, Integer> sortMap(final Map<String, Integer> map) {
        if (map == null) {
            return null;
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        entries = entries.subList(0, Math.min(entries.size(), SearchBarConstants.RESULTS_MAX_SIZE));

        Map<String, Integer> result = new LinkedHashMap<>();
        entries.forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        return result;
    }

    /**
     * Sort all stored maps by the values
     */
    private void sortMaps() {
        topArtists = sortMap(topArtists);
        topAlbums = sortMap(topAlbums);
        topGenres = sortMap(topGenres);
        topSongs = sortMap(topSongs);
        topEpisodes = sortMap(topEpisodes);
    }

    /**
     * Increment the key count
     *
     * @param map   the map
     * @param key   the key
     * @param count key add count
     */
    private void addToKey(final Map<String, Integer> map, final String key, final int count) {
        map.merge(key, count, Integer::sum);
    }

    @Override
    public void visit(final User user) {
        if (user.getListenedSongs().isEmpty() && user.getListenedEpisodes().isEmpty()) {
            empty = true;
            return;
        }

        prepareForUser();

        user.getListenedSongs().forEach((key, value) -> {
            addToKey(topSongs, key.getName(), value);
            addToKey(topArtists, key.getArtist(), value);
            addToKey(topAlbums, key.getAlbum(), value);
            addToKey(topGenres, key.getGenre(), value);
        });

        user.getListenedEpisodes().forEach((key, value) ->
                addToKey(topEpisodes, key.getName(), value)
        );

        sortMaps();
    }

    @Override
    public void visit(final Host host) {
        prepareForHost();

        Map<String, Integer> fans = new LinkedHashMap<>();

        for (User user : UserDatabase.getInstance().getUsers()) {
            for (var entry : user.getListenedEpisodes().entrySet()) {
                if (host.hostsEpisode(entry.getKey())) {
                    addToKey(topEpisodes, entry.getKey().getName(), entry.getValue());
                    addToKey(fans, user.getName(), entry.getValue());
                }
            }
        }

        listeners = fans.entrySet().size();
        sortMaps();

        if (listeners == 0) {
            empty = true;
        }
    }

    @Override
    public void visit(final Artist artist) {
        prepareForArtist();

        Map<String, Integer> fans = new LinkedHashMap<>();
        for (User user : UserDatabase.getInstance().getUsers()) {
            for (var entry : user.getListenedSongs().entrySet()) {
                if (entry.getKey().getArtist().equals(artist.getName())) {
                    addToKey(topAlbums, entry.getKey().getAlbum(), entry.getValue());
                    addToKey(topSongs, entry.getKey().getName(), entry.getValue());
                    addToKey(fans, user.getName(), entry.getValue());
                }
            }
        }

        listeners = fans.entrySet().size();

        /* Not using Artist.getTop5Fans because the previous for's would be done twice */
        fans = sortMap(fans);
        fans.forEach((key, value) -> topFans.add(key));

        sortMaps();

        if (listeners == 0) {
            empty = true;
        }
    }
}
