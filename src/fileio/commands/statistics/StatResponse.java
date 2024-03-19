package fileio.commands.statistics;

import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.Response;
import fileio.commands.utils.stats.StatResult;
import fileio.commands.utils.constants.ResponseConstants;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Overall statistics response class
 */
public final class StatResponse extends Response {
    private StatResult result = new StatResult();
    private String userType;

    public StatResponse(final CommandWrapper input) {
        super(input);
    }

    @Override
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());
        Host host = userDatabase.getHostByName(input.getUsername());
        Artist artist = userDatabase.getArtistByName(input.getUsername());

        userDatabase.updateAllPlayers(input.getTimestamp());

        userType = "user";
        if (user != null) {
            user.accept(result);
        } else if (host != null) {
            userType = "host";
            host.accept(result);
        } else {
            userType = "artist";
            artist.accept(result);
        }

        if (result.isEmpty()) {
            setMessage(ResponseConstants.STAT_NO_DATA + userType + " " + input.getUsername() + ".");
            result = null;
        }
    }

    @Override
    public JsonNode toNode() {
        ObjectNode objectNode = (ObjectNode) super.toNode();

        if (result != null) {
            objectNode.putPOJO("result", result.toNode(userType));
        }

        return objectNode;
    }
}
