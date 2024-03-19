package fileio.commands.handler;

import factory.response.AbstractResponseFactory;
import factory.response.ResponseFactory;
import fileio.commands.Response;
import fileio.commands.wrapper.CommandWrapper;
import databases.Library;
import databases.UserDatabase;

/**
 * Handles all command cases and returns the corresponding response
 */
public final class CommandHandler {
    private CommandHandler() {
    }

    /**
     * Executes the given input command
     *
     * @param command the command
     * @return The corresponding response
     */
    public static Response executeCommand(final CommandWrapper command) {
        AbstractResponseFactory abstractFactory = AbstractResponseFactory.getInstance();
        ResponseFactory factory = abstractFactory.createObject(new Object[]{command.getCommand()});
        Response response = factory.createObject(new Object[]{command});

        response.getResponse(Library.getInstance(), UserDatabase.getInstance(), command);
        return response;
    }
}
