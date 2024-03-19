package factory.response;

import fileio.commands.Response;
import fileio.commands.statistics.InfoResponse;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Factory for info responses
 */
public final class InfoResponseFactory extends ResponseFactory {
    public InfoResponseFactory() {
    }

    @Override
    public Response createObject(final Object[] data) {
        return new InfoResponse((CommandWrapper) data[0]);
    }
}
