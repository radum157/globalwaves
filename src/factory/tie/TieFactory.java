package factory.tie;

import app.audiofiles.Song;
import app.audiofiles.collections.Album;
import app.audiofiles.collections.Podcast;
import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;
import fileio.commands.wrapper.CommandWrapper;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that creates different types of TiedEntities
 */
public final class TieFactory {
    public TieFactory() {
    }

    /**
     * Helper for user creations
     *
     * @param input the parameters
     * @return A new UserInput object
     */
    private UserInput getUserInput(final CommandWrapper input) {
        UserInput userInput = new UserInput();

        userInput.setAge(input.getAge());
        userInput.setCity(input.getCity());
        userInput.setUsername(input.getUsername());

        return userInput;
    }

    /**
     * Creates a new User from an input command
     *
     * @param input the parameters
     * @return The newly-created user
     */
    public User createUser(final CommandWrapper input) {
        return new User(getUserInput(input));
    }

    /**
     * Creates a new Artist from an input command
     *
     * @param input the parameters
     * @return The newly-created Artist
     */
    public Artist createArtist(final CommandWrapper input) {
        return new Artist(getUserInput(input));
    }

    /**
     * Creates a new Host from an input command
     *
     * @param input the parameters
     * @return The newly-created Host
     */
    public Host createHost(final CommandWrapper input) {
        return new Host(getUserInput(input));
    }

    /**
     * Creates a new album from a command
     *
     * @param input the parameters
     * @return The new album
     */
    public Album createAlbum(final CommandWrapper input) {
        List<Song> songs = new ArrayList<>();
        for (SongInput songInput : input.getSongs()) {
            songs.add(new Song(songInput));
        }

        Album album = new Album(
                input.getName(),
                input.getUsername(),
                input.getDescription(),
                input.getReleaseYear()
        );

        album.getSongs().addAll(songs);
        return album;
    }

    /**
     * Creates a new podcast from a command
     *
     * @param input the parameters
     * @return The new podcast
     */
    public Podcast createPodcast(final CommandWrapper input) {
        PodcastInput podcastInput = new PodcastInput();

        podcastInput.setName(input.getName());
        podcastInput.setOwner(input.getUsername());
        podcastInput.setEpisodes(input.getEpisodes());

        return new Podcast(podcastInput);
    }
}
