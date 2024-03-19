package fileio.commands;

import app.admin.Admin;
import app.mediaplayer.MediaPlayer;
import app.page.handler.PageHandler;
import app.searchbar.SearchBar;
import app.users.normal.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.wrapper.CommandWrapper;
import lombok.Getter;
import lombok.Setter;

/**
 * Standard class for responding to an input command.
 */
public class Response {
    private final String command;
    private String username;
    private Integer timestamp;

    @Getter
    @Setter
    private String message;

    public Response(final Command command) {
        this.username = command.getUsername();
        this.command = command.getCommand();
        this.timestamp = command.getTimestamp();
    }

    public Response(final String command) {
        this.command = command;
    }

    /**
     * Handles user-related commands
     *
     * @param library      the library
     * @param userDatabase the user database
     * @param input        the command
     */
    private void handleUserCommands(final Library library, final UserDatabase userDatabase,
                                    final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        if (user == null) {
            return;
        }

        MediaPlayer mediaPlayer = user.getMediaPlayer();
        SearchBar searchBar = user.getSearchBar();

        message = switch (input.getCommand()) {
            case "select" -> searchBar.doSelect(input.getItemNumber(), user);
            case "load" -> mediaPlayer.loadFile(
                   searchBar.popSelection(), searchBar.syncState(), input.getTimestamp()
            );
            case "playPause" -> mediaPlayer.playPauseFile(input.getTimestamp());
            case "next" -> mediaPlayer.playNext(input.getTimestamp());
            case "prev" -> mediaPlayer.playPrevious(input.getTimestamp());
            case "repeat" -> mediaPlayer.repeatFile(input.getTimestamp());
            case "like" -> mediaPlayer.likeSong(user, input.getTimestamp());
            case "shuffle" -> mediaPlayer.shuffleLoadedFile(
                    input.getSeed(), input.getTimestamp()
            );
            case "forward", "backward" -> mediaPlayer.executeSkip(
                    input.getCommand(), input.getTimestamp()
            );
            case "addRemoveInPlaylist" -> mediaPlayer.addRemoveInPlaylist(
                    user, input.getTimestamp(), input.getPlaylistId()
            );
            case "follow" -> searchBar.followPlaylist(user);
            case "createPlaylist" -> user.createPlaylist(input.getPlaylistName(), library);
            case "switchVisibility" -> user.switchPlaylistPrivacy(input.getPlaylistId());
            case "loadRecommendations" -> user.loadRecommendation(input.getTimestamp());
            default -> null;
        };
    }

    /**
     * Handles page-related commands
     *
     * @param userDatabase the user database
     * @param input        the command
     */
    private void handlePageCommands(final UserDatabase userDatabase, final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        PageHandler pageHandler = new PageHandler();

        message = switch (input.getCommand()) {
            case "changePage" -> pageHandler.changePage(
                    user, input.getNextPage(), input.getTimestamp()
            );
            case "printCurrentPage" -> pageHandler.printPage(user);
            case "addMerch" -> pageHandler.addMerchTo(userDatabase, input);
            case "addAnnouncement" -> pageHandler.addAnnouncementTo(userDatabase, input);
            case "removeAnnouncement" -> pageHandler.removeAnnouncementFrom(userDatabase, input);
            case "addEvent" -> pageHandler.addEventTo(userDatabase, input);
            case "removeEvent" -> pageHandler.removeEventFrom(userDatabase, input);
            case "nextPage", "previousPage" -> pageHandler.browsePages(user, input);
            default -> null;
        };
    }

    /**
     * Executes admin commands
     *
     * @param userDatabase the user database
     * @param input        the command
     */
    private void handleAdminCommands(final UserDatabase userDatabase, final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        Admin admin = new Admin();

        message = switch (input.getCommand()) {
            case "addUser" -> admin.addUser(input);
            case "deleteUser" -> admin.deleteUser(input);
            case "addAlbum" -> admin.addAlbum(input);
            case "removeAlbum" -> admin.removeAlbum(input);
            case "addPodcast" -> admin.addPodcast(input);
            case "removePodcast" -> admin.removePodcast(input);
            case "switchConnectionStatus" -> admin.switchConnectionOf(
                    input.getUsername(), input.getTimestamp()
            );
            case "buyPremium", "cancelPremium" -> admin.switchPremium(
                    user, input
            );
            case "adBreak" -> admin.insertAd(user, input);
            case "buyMerch" -> admin.doBuyMerch(user, input);
            case "subscribe" -> admin.addSubscription(user, input);
            case "updateRecommendations" -> admin.updateRecommendations(input);
            default -> null;
        };
    }

    /**
     * Executes the command and sets the response variables.
     * <p>
     * The parameter list includes all useful data for the command execution
     * (to reduce access to the databases).
     *
     * @param input the command
     */
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        /* Split into cases */
        handleUserCommands(library, userDatabase, input);
        if (message == null) {
            handlePageCommands(userDatabase, input);
        }
        if (message == null) {
            handleAdminCommands(userDatabase, input);
        }
    }

    /**
     * Creates an object node for JSON printing with Jackson
     *
     * @return the JSON node
     */
    public JsonNode toNode() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("command", command);
        if (username != null) {
            objectNode.put("user", username);
        }
        if (timestamp != null) {
            objectNode.put("timestamp", timestamp);
        }
        if (message != null) {
            objectNode.put("message", message);
        }

        return objectNode;
    }
}
