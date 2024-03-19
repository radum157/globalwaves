package fileio.commands.statistics;

import app.audiofiles.Song;
import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;
import fileio.commands.utils.formats.AlbumFormat;
import fileio.commands.utils.formats.NotificationFormat;
import fileio.commands.utils.formats.PlaylistFormat;
import fileio.commands.utils.formats.PodcastFormat;
import app.utils.page.data.Merchandise;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.wrapper.CommandWrapper;
import fileio.commands.Command;
import fileio.commands.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response class to all statistics commands
 */
public final class InfoResponse extends Response {
    private String jsonName = "result";
    private final List<Object> result = new ArrayList<>();

    public InfoResponse(final Command command) {
        super(command);
    }

    /**
     * Executes commands that return formatted outputs.
     * Receives the relevant parameters from getResponse.
     */
    private void getFormattedResults(final User user, final Artist artist, final Host host,
                                     final CommandWrapper command) {
        if (command.getCommand().equals("getNotifications")) {
            jsonName = "notifications";
        }

        var results = switch (command.getCommand()) {
            case "showPlaylists" -> PlaylistFormat.formatList(user.getPlaylists());
            case "showAlbums" -> AlbumFormat.formatList(artist.getAlbums());
            case "showPodcasts" -> PodcastFormat.formatList(host.getPodcasts());
            case "getNotifications" -> NotificationFormat.formatList(user.popNotifications());
            default -> null;
        };

        if (results != null) {
            result.addAll(results);
        }
    }

    /**
     * Executes commands that return named outputs.
     * Receives the relevant parameters from getResponse.
     */
    private void getNamedResults(final User user, final Library library,
                                 final UserDatabase userDatabase,
                                 final CommandWrapper command) {
        /* Named outputs */
        List<String> results = switch (command.getCommand()) {
            case "showPreferredSongs" -> user.getLikedSongs().stream().map(Song::getName)
                    .collect(Collectors.toList());
            case "getTop5Songs" -> library.getTop5Songs();
            case "getTop5Playlists" -> library.getTop5Playlists();
            case "getAllUsers" -> userDatabase.getAllUsers();
            case "getOnlineUsers" -> userDatabase.getOnlineUsers();
            case "getTop5Albums" -> library.getTop5Albums();
            case "getTop5Artists" -> userDatabase.getTop5Artists();
            case "seeMerch" -> user.getBoughtMerch().stream().map(Merchandise::name)
                    .collect(Collectors.toList());
            default -> null;
        };

        if (results != null) {
            result.addAll(results);
        }
    }

    @Override
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        Artist artist = userDatabase.getArtistByName(input.getUsername());
        Host host = userDatabase.getHostByName(input.getUsername());

        /* Split for future refactoring or extension */
        getFormattedResults(user, artist, host, input);
        if (result.isEmpty()) {
            getNamedResults(user, library, userDatabase, input);
        }
    }

    @Override
    public JsonNode toNode() {
        ObjectNode objectNode = (ObjectNode) super.toNode();

        objectNode.putPOJO(jsonName, result);

        return objectNode;
    }
}
