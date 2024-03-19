package fileio.commands;

import lombok.Getter;
import lombok.Setter;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Standard input command template.
 */
@Getter
public abstract class Command {
    private String command;
    @Setter
    private String username;
    private int timestamp;

    public Command() {
    }

    public Command(final CommandWrapper input) {
        username = input.getUsername();
        command = input.getCommand();
        timestamp = input.getTimestamp();
    }
}
