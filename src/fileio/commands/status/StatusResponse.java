package fileio.commands.status;

import app.users.normal.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.wrapper.CommandWrapper;
import fileio.commands.Command;
import fileio.commands.Response;
import app.mediaplayer.MediaPlayerStats;

/**
 * Response class for player status interrogations
 */
public final class StatusResponse extends Response {
    private MediaPlayerStats stats;

    public StatusResponse(final Command command) {
        super(command);
    }

    @Override
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        stats = user.getMediaPlayer().getPlayerStatus(input);
    }

    @Override
    public JsonNode toNode() {
        ObjectNode objectNode = (ObjectNode) super.toNode();

        objectNode.putPOJO("stats", stats);

        return objectNode;
    }
}
