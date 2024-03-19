package fileio.commands.wrapper;

import fileio.commands.search.SearchCommand;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import fileio.commands.Command;

import java.util.ArrayList;

/**
 * Wrapper used for JSON parsing of any type of command
 */
@Getter
public final class CommandWrapper extends Command {
    /* Everything inherited is common */

    /* Search filters */
    private SearchCommand.SearchFilter filters;

    /* Selection id */
    private Integer itemNumber;

    /* Playlist data */
    private String playlistName;
    private Integer playlistId;

    /* Shuffle argument */
    private Integer seed;

    /* Page data */
    private String nextPage;

    /* User data */
    private String type;
    private Integer age;
    private String city;

    /* Album data */
    private String name;
    private Integer releaseYear;
    private String description;
    private ArrayList<SongInput> songs;

    /* Announcement date */
    private String date;

    /* Merch price */
    private Integer price;

    /* Podcast data */
    private ArrayList<EpisodeInput> episodes;

    /**/
    private String recommendationType;
}
