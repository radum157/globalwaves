package app.audiofiles.collections;

import databases.UserDatabase;
import app.mediaplayer.MediaPlayer;
import app.users.normal.User;
import app.utils.audiostate.PlaylistState;
import lombok.Getter;
import app.audiofiles.AudioFile;
import app.audiofiles.Song;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.player.MediaPlayerConstants;
import app.utils.constants.audio.PlaylistConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Playlist class.
 */
@Getter
public class Playlist implements AudioFile {
    private String name;
    private final String owner;

    protected final List<Song> songs = new ArrayList<>();
    private boolean isPrivate;

    private int followers;

    public Playlist(final String name, final String owner, final boolean isPrivate) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.owner = owner;
    }

    /**
     * The standard playlist addition of ties goes through all songs
     * <p>
     * Consider overriding if this is not necessary
     */
    @Override
    public void addTie() {
        User user = UserDatabase.getInstance().getUserByName(owner);
        if (user != null) {
            user.addTie();
        }

        for (Song song : songs) {
            song.addTie();
        }
    }

    /**
     * Complementary to addTie
     */
    @Override
    public void removeTie() {
        User user = UserDatabase.getInstance().getUserByName(owner);
        if (user != null) {
            user.removeTie();
        }

        for (Song song : songs) {
            song.removeTie();
        }
    }

    @Override
    public final String getFileOwner(final AudioFileState currentState) {
        if (currentState.isOver()) {
            return null;
        }

        PlaylistState playlistState = (PlaylistState) currentState;
        int index = (playlistState.isShuffled())
                ? playlistState.getShuffleIndexes()
                .indexOf(playlistState.getLastIndex())
                : playlistState.getLastIndex();

        return songAt(index, playlistState.getShuffleIndexes()).getArtist();
    }

    @Override
    public final List<Integer> shuffleFile(final Integer seed) {
        if (seed == null) {
            return null;
        }

        ArrayList<Integer> shuffleIndexes =
                IntStream.range(0, songs.size()).boxed()
                        .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(shuffleIndexes, new Random(seed));

        return shuffleIndexes;
    }

    @Override
    public final AudioFile getCurrentFile(final AudioFileState currentState) {
        PlaylistState playlistState = (PlaylistState) currentState;
        if (playlistState.isShuffled()) {
            return songAt(playlistState.getLastIndex(), playlistState.getShuffleIndexes());
        }
        return songs.get(currentState.getLastIndex());
    }

    @Override
    public final AudioFileState getInitialState() {
        PlaylistState state = new PlaylistState();

        MediaPlayer.setDefaultState(state);
        state.setLastDuration(songs.get(0).getDuration());

        return state;
    }

    @Override
    public final String getRepeatModeAsString(final int repeatMode) {
        if (repeatMode == MediaPlayerConstants.REPEAT_NONE) {
            return MediaPlayerConstants.NO_REPEAT_MESSAGE;
        }
        if (repeatMode == MediaPlayerConstants.REPEAT_ALL_ONCE) {
            return MediaPlayerConstants.REPEAT_ALL_MESSAGE;
        }
        return MediaPlayerConstants.REPEAT_CURRENT_MESSAGE;
    }

    /**
     * @param index          the normal index
     * @param shuffleIndexes the shuffle index array to use
     * @return The song at the given index, taking into account the shuffle status
     */
    public Song songAt(final int index, final List<Integer> shuffleIndexes) {
        return songs.get((shuffleIndexes != null) ? shuffleIndexes.get(index) : index);
    }

    @Override
    public final String getPrevious(final AudioFileState currentState, final User user) {
        PlaylistState playlistState = (PlaylistState) currentState;
        List<Integer> shuffleIndexes = playlistState.getShuffleIndexes();

        int index = (playlistState.isShuffled())
                ? shuffleIndexes.indexOf(playlistState.getLastIndex())
                : playlistState.getLastIndex();

        if (playlistState.getTimePosition() > 0 || index == 0) {
            playlistState.setTimePosition(0);
            return songs.get(playlistState.getLastIndex()).getName();
        }

        playlistState.setLastIndex((playlistState.isShuffled())
                ? shuffleIndexes.get(index - 1) : index - 1);
        playlistState.setLastDuration(songs.get(playlistState.getLastIndex()).getDuration());

        index = (playlistState.isShuffled())
                ? shuffleIndexes.indexOf(playlistState.getLastIndex())
                : playlistState.getLastIndex();
        user.listenTo(songAt(index, playlistState.getShuffleIndexes()), 1);

        return songs.get(playlistState.getLastIndex()).getName();
    }

    @Override
    public final void simulateTime(final AudioFileState currentState, final int elapsedTime,
                                   final User user) {
        PlaylistState playlistState = (PlaylistState) currentState;
        int timePosition = playlistState.getTimePosition() + elapsedTime;
        boolean shuffled = playlistState.isShuffled();
        List<Integer> shuffleIndexes = (shuffled) ? playlistState.getShuffleIndexes() : null;

        int i = (shuffled)
                ? shuffleIndexes.indexOf(playlistState.getLastIndex())
                : playlistState.getLastIndex();
        Song song = songAt(i, shuffleIndexes);

        if (playlistState.getRepeatMode() == MediaPlayerConstants.REPEAT_CURRENT_INFINITE) {
            playlistState.setTimePosition(timePosition % song.getDuration());
            return;
        }

        if (timePosition != 0 || user.getMediaPlayer().getAdTime() == 0) {
            user.listenTo(song, -1);
        }

        /* songs.size == shuffleIndexes.size so no special cases */
        while (i < songs.size()) {
            song = songAt(i, shuffleIndexes);

            if (timePosition < song.getDuration()) {
                if (timePosition != 0 || user.getMediaPlayer().getAdTime() == 0) {
                    user.listenTo(song, 1);
                }

                playlistState.setLastIndex((shuffled) ? shuffleIndexes.get(i) : i);
                playlistState.setTimePosition(timePosition);
                playlistState.setLastDuration(song.getDuration());
                return;
            }

            user.listenTo(song, 1);
            timePosition -= song.getDuration();
            i = (playlistState.getRepeatMode() == MediaPlayerConstants.REPEAT_ALL_ONCE)
                    ? (i + 1) % songs.size() : i + 1;
        }

        /* Repeat none and played until the end */
        playlistState.clear();
    }

    @Override
    public final boolean isEmpty() {
        return songs.isEmpty();
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * @param song the song to be added
     */
    public void addSong(final Song song) {
        songs.add(song);
    }

    /**
     * @param song the song to be removed
     */
    public void removeSong(final Song song) {
        songs.remove(song);
    }

    @Override
    public final String addFollow() {
        followers++;
        return PlaylistConstants.FOLLOW_SUCCESS;
    }

    @Override
    public final String removeFollow() {
        followers--;
        return PlaylistConstants.UNFOLLOW_SUCCESS;
    }

    /**
     * @return The total amount of likes the songs have accumulated
     */
    public int getLikes() {
        return songs.stream().mapToInt(Song::getLikes).sum();
    }

    /**
     * Changes the visibility
     */
    public void switchPrivacy() {
        isPrivate = !isPrivate;
    }
}
