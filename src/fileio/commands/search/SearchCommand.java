package fileio.commands.search;

import lombok.Getter;
import fileio.commands.Command;
import fileio.commands.wrapper.CommandWrapper;

import java.util.ArrayList;

@Getter
public class SearchCommand extends Command {
    private final String type;
    private final SearchFilter filters;

    public SearchCommand(final CommandWrapper input) {
        super(input);
        type = input.getType();
        filters = input.getFilters();
    }

    /**
     * Class used for JSON parsing of SearchCommand, containing all possible search filters
     */
    @Getter
    public static final class SearchFilter {
        private String name;
        private String owner;
        private String username;
        private ArrayList<String> tags;
        private String lyrics;
        private String releaseYear;
        private String artist;
        private String album;
        private String genre;
        private String description;

        private SearchFilter() {
        }

        /**
         * @return The integer part of the year
         */
        public int getYearAsInt() {
            return Integer.parseInt(releaseYear.substring(1));
        }
    }
}
