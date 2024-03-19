package app.audiofiles;

import app.audiofiles.collections.Album;
import app.users.normal.User;
import databases.Library;
import app.mediaplayer.MediaPlayer;
import fileio.commands.statistics.EndProgramResponse;
import fileio.input.SongInput;
import lombok.Getter;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.player.MediaPlayerConstants;
import app.utils.constants.users.UserConstants;

import java.util.ArrayList;
import java.util.Objects;

/**
 * SongInput wrapper with added functionalities.
 * In all AudioFile collection operations,
 * it is considered a collection only containing the song itself
 */
public final class Song implements AudioFile {
    private final SongInput songInput;
    @Getter
    private int likes;

    public Song(final SongInput songInput) {
        this.songInput = songInput;
    }

    /**
     * Adds to total song revenue
     *
     * @param value the value to be added
     */
    public void addRevenue(final double value) {
        EndProgramResponse.getSongRevenues().merge(this, value, Double::sum);
    }

    @Override
    public String addLike() {
        likes++;
        return UserConstants.LIKE_SUCCESS;
    }

    @Override
    public String removeLike() {
        likes--;
        return UserConstants.DISLIKE_SUCCESS;
    }

    @Override
    public String getFileOwner(final AudioFileState currentState) {
        if (currentState.isOver()) {
            return null;
        }
        return getArtist();
    }

    @Override
    public boolean canAddToCollection() {
        return true;
    }

    @Override
    public String getRepeatModeAsString(final int repeatMode) {
        if (repeatMode == 0) {
            return MediaPlayerConstants.NO_REPEAT_MESSAGE;
        }
        if (repeatMode == 1) {
            return MediaPlayerConstants.REPEAT_ONCE_MESSAGE;
        }
        return MediaPlayerConstants.REPEAT_INFINITE_MESSAGE;
    }

    @Override
    public String getPrevious(final AudioFileState currentState, final User user) {
        currentState.setState(
                0,
                0,
                songInput.getDuration(),
                false
        );

        return songInput.getName();
    }

    @Override
    public void addTie() {
        Album album = Library.getInstance().getAlbumByName(songInput.getAlbum());
        if (album != null) {
            album.addTie();
        }
    }

    @Override
    public void removeTie() {
        Album album = Library.getInstance().getAlbumByName(songInput.getAlbum());
        if (album != null) {
            album.removeTie();
        }
    }

    @Override
    public AudioFileState getInitialState() {
        AudioFileState state = new AudioFileState();

        MediaPlayer.setDefaultState(state);
        state.setLastDuration(songInput.getDuration());

        return state;
    }

    @Override
    public void simulateTime(final AudioFileState currentState, final int elapsedTime,
                             final User user) {
        int timePosition = currentState.getTimePosition() + elapsedTime;
        if (timePosition < songInput.getDuration()) {
            currentState.setTimePosition(timePosition);
            return;
        }

        if (currentState.getRepeatMode() == MediaPlayerConstants.REPEAT_NONE) {
            currentState.clear();
            return;
        }

        currentState.setTimePosition(timePosition % songInput.getDuration());

        if (currentState.getRepeatMode() == MediaPlayerConstants.REPEAT_ALL_ONCE) {
            currentState.setRepeatMode(MediaPlayerConstants.REPEAT_NONE);
            currentState.setRepeatModeAsString(
                    getRepeatModeAsString(currentState.getRepeatMode())
            );
        }
    }

    @Override
    public String getName() {
        return songInput.getName();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return The album the song is a part of
     */
    public String getAlbum() {
        return songInput.getAlbum();
    }

    /**
     * @return The artist who sings the song
     */
    public String getArtist() {
        return songInput.getArtist();
    }

    /**
     * @return The genre of the song
     */
    public String getGenre() {
        return songInput.getGenre();
    }

    /**
     * @return The lyrics associated with the song
     */
    public String getLyrics() {
        return songInput.getLyrics();
    }

    /**
     * @return The duration of the song
     */
    public int getDuration() {
        return songInput.getDuration();
    }

    /**
     * @return The release year of the song
     */
    public int getReleaseYear() {
        return songInput.getReleaseYear();
    }

    /**
     * @return The tags associated with the song
     */
    public ArrayList<String> getTags() {
        return songInput.getTags();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Song song = (Song) object;
        return songInput.equals(song.songInput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songInput);
    }
}
