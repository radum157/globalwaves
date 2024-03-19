package fileio.commands.search;

import app.users.normal.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.wrapper.CommandWrapper;
import lombok.Getter;
import fileio.commands.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to generate a response to SearchInputCommand
 */
@Getter
public final class SearchResponse extends Response {
    private List<String> results;

    public SearchResponse(final SearchCommand command) {
        super(command);
    }

    /**
     * Deep-copy setter
     *
     * @param target the target result list
     */
    public void setResults(final List<String> target) {
        if (target != null) {
            results = new ArrayList<>(target);
        }
    }

    @Override
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        User user = userDatabase.getUserByName(input.getUsername());

        SearchResponse response = user.getSearchBar().doSearch(
                new SearchCommand(input), library, user, userDatabase
        );

        setMessage(response.getMessage());
        results = response.getResults();
    }

    @Override
    public JsonNode toNode() {
        ObjectNode objectNode = (ObjectNode) super.toNode();

        objectNode.putPOJO("results", results);

        return objectNode;
    }
}
