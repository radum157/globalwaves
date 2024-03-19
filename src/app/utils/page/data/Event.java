package app.utils.page.data;

import fileio.commands.wrapper.CommandWrapper;
import app.utils.constants.page.PageConstants;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Event record
 *
 * @param name the name
 * @param date the publishing date
 * @param description the description
 */
public record Event(String name, LocalDate date, String description) {
    /**
     * Creates a new event from a JSON input
     *
     * @param command the parameters
     * @return The event or null if the date is invalid
     */
    public static Event parseFromCommand(final CommandWrapper command) {
        try {
            return new Event(
                    command.getName(),
                    LocalDate.parse(command.getDate(), PageConstants.EVENT_DATE_FORMAT),
                    command.getDescription()
            );
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
