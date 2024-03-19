package factory.response;

import fileio.commands.Response;
import fileio.commands.status.StatusResponse;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Factory for status responses
 */
public final class StatusResponseFactory extends ResponseFactory {
    public StatusResponseFactory() {
    }

    @Override
    public Response createObject(final Object[] data) {
        return new StatusResponse((CommandWrapper) data[0]);
    }
}
