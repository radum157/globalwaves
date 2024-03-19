package factory.response;

import fileio.commands.Response;
import fileio.commands.statistics.StatResponse;
import fileio.commands.wrapper.CommandWrapper;

public final class StatResponseFactory extends ResponseFactory {
    public StatResponseFactory() {
    }

    @Override
    public Response createObject(final Object[] data) {
        return new StatResponse((CommandWrapper) data[0]);
    }
}
