package app.utils.audiostate;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class used to store playlist and album states
 */
@Getter
@Setter
public final class PlaylistState extends AudioFileState {
    private List<Integer> shuffleIndexes;
    private boolean shuffled;

    @Override
    public void clear() {
        super.clear();
        shuffled = false;
    }

    /**
     * @return If the playlist is shuffled
     */
    @Override
    public boolean isShuffled() {
        return shuffled;
    }
}
