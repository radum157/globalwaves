package factory.response;

import fileio.commands.Response;
import fileio.commands.search.SearchCommand;
import fileio.commands.search.SearchResponse;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Factory for search responses
 */
public final class SearchResponseFactory extends ResponseFactory {
    public SearchResponseFactory() {
    }

    @Override
    public Response createObject(final Object[] data) {
        return new SearchResponse(new SearchCommand((CommandWrapper) data[0]));
    }
}
