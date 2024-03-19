package app.utils.page.data;

import fileio.commands.wrapper.CommandWrapper;

/**
 * Announcement record
 *
 * @param name the name
 * @param description the description
 */
public record Announcement(String name, String description) {
    /**
     * Creates a new announcement from a JSON input
     *
     * @param command the parameters
     * @return The announcement
     */
    public static Announcement parseFromCommand(final CommandWrapper command) {
        return new Announcement(command.getName(), command.getDescription());
    }
}
