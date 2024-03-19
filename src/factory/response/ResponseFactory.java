package factory.response;

import factory.Factory;
import fileio.commands.Response;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Factory for standard responses.
 */
public class ResponseFactory implements Factory<Response> {
    public ResponseFactory() {
    }

    /**
     * Creates a new standard response. Override for more complex response cases.
     */
    @Override
    public Response createObject(final Object[] data) {
        return new Response((CommandWrapper) data[0]);
    }
}
