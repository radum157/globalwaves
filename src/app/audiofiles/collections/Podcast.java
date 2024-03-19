package app.audiofiles.collections;

import app.users.normal.User;
import databases.UserDatabase;
import app.mediaplayer.MediaPlayer;
import app.users.creators.Host;
import app.utils.audiostate.PodcastState;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import app.audiofiles.AudioFile;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.player.MediaPlayerConstants;
import app.utils.constants.audio.PodcastConstants;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * Podcast class. Wrapper for PodcastInput.
 */
public final class Podcast implements AudioFile {
    private final PodcastInput podcastInput;
    @Getter
    private int ties;

    public Podcast(final PodcastInput podcastInput) {
        this.podcastInput = podcastInput;
    }

    @Override
    public String getPrevious(final AudioFileState currentState, final User user) {
        if (currentState.getTimePosition() > 0 || currentState.getLastIndex() == 0) {
            currentState.setTimePosition(0);
            return podcastInput.getEpisodes().get(currentState.getLastIndex()).getName();
        }

        currentState.setLastIndex(currentState.getLastIndex() - 1);
        currentState.setLastDuration(
                podcastInput.getEpisodes().get(currentState.getLastIndex()).getDuration()
        );
        user.listenTo(this, currentState.getLastIndex(), 1);

        return podcastInput.getEpisodes().get(currentState.getLastIndex()).getName();
    }

    @Override
    public String getRepeatModeAsString(final int repeatMode) {
        if (repeatMode == MediaPlayerConstants.REPEAT_NONE) {
            return MediaPlayerConstants.NO_REPEAT_MESSAGE;
        }
        if (repeatMode == MediaPlayerConstants.REPEAT_ALL_ONCE) {
            return MediaPlayerConstants.REPEAT_ONCE_MESSAGE;
        }
        return MediaPlayerConstants.REPEAT_INFINITE_MESSAGE;
    }

    @Override
    public String skipForward(final AudioFileState currentState, final User user) {
        simulateTime(currentState, PodcastConstants.SKIP_SECOND_COUNT, user);
        return PodcastConstants.SKIP_MESSAGE;
    }

    @Override
    public String skipBackward(final AudioFileState currentState) {
        currentState.setTimePosition(
                Math.max(0, currentState.getTimePosition() - PodcastConstants.SKIP_SECOND_COUNT)
        );
        currentState.setOver(false);

        return PodcastConstants.REWIND_MESSAGE;
    }

    @Override
    public void addTie() {
        Host host = UserDatabase.getInstance().getHostByName(podcastInput.getOwner());
        if (host != null) {
            host.addTie();
            ties++;
        }
    }

    @Override
    public void removeTie() {
        Host host = UserDatabase.getInstance().getHostByName(podcastInput.getOwner());
        if (host != null) {
            host.removeTie();
            ties = Math.max(0, ties - 1);
        }
    }

    @Override
    public AudioFileState getInitialState() {
        PodcastState state = new PodcastState(podcastInput.getName());

        MediaPlayer.setDefaultState(state);
        state.setLastDuration(podcastInput.getEpisodes().get(0).getDuration());

        return state;
    }

    @Override
    public String getCurrentName(final AudioFileState currentState) {
        return podcastInput.getEpisodes().get(
                currentState.getLastIndex() % podcastInput.getEpisodes().size()
        ).getName();
    }

    @Override
    public void simulateTime(final AudioFileState currentState, final int elapsedTime,
                             final User user) {
        int timePosition = currentState.getTimePosition() + elapsedTime;
        List<EpisodeInput> episodes = podcastInput.getEpisodes();

        user.listenTo(this, currentState.getLastIndex(), -1);

        for (int i = currentState.getLastIndex(); i < episodes.size();) {
            user.listenTo(this, i, 1);

            if (timePosition < episodes.get(i).getDuration()) {
                currentState.setState(i, timePosition, episodes.get(i).getDuration(), false);
                return;
            }

            timePosition -= episodes.get(i).getDuration();
            i = (currentState.getRepeatMode() == MediaPlayerConstants.REPEAT_CURRENT_INFINITE)
                    ? (i + 1) % episodes.size() : i + 1;
        }

        /* Reset the state for repeat once */
        if (currentState.getRepeatMode() == MediaPlayerConstants.REPEAT_ALL_ONCE) {
            currentState.setRepeatMode(MediaPlayerConstants.REPEAT_NONE);
            currentState.setLastIndex(0);
            currentState.setTimePosition(0);

            simulateTime(currentState, timePosition, user);
        }

        /* Cannot clear the state because of skip commands and the need to save podcast states */
        currentState.setState(
                episodes.size() - 1,
                episodes.get(episodes.size() - 1).getDuration(),
                episodes.get(episodes.size() - 1).getDuration(),
                true
        );
    }

    @Override
    public boolean isEmpty() {
        return podcastInput.getEpisodes().isEmpty();
    }

    @Override
    public String getName() {
        return podcastInput.getName();
    }

    @Override
    public String getFileOwner(final AudioFileState currentState) {
        if (currentState.isOver()) {
            return null;
        }
        return getOwner();
    }

    /**
     * @return The owner of the podcast
     */
    public String getOwner() {
        return podcastInput.getOwner();
    }

    /**
     * @return The podcast episodes
     */
    public List<EpisodeInput> getEpisodes() {
        return podcastInput.getEpisodes();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Podcast podcast = (Podcast) object;
        return podcastInput == podcast.podcastInput;
    }

    @Override
    public int hashCode() {
        return Objects.hash(podcastInput);
    }
}
