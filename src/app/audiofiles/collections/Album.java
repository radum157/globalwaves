package app.audiofiles.collections;

import databases.UserDatabase;
import app.users.creators.Artist;
import lombok.Getter;

/**
 * Album class. Extension to Playlist. Counts ties for future deletions.
 */
@Getter
public final class Album extends Playlist {
    private final String description;
    private final int year;
    private int ties;

    public Album(final String name, final String owner,
                 final String description, final int releaseYear) {
        super(name, owner, false);
        this.description = description;
        this.year = releaseYear;
    }

    /**
     * Note that only the currently playing song will be added
     */
    @Override
    public boolean canAddToCollection() {
        return true;
    }

    @Override
    public void addTie() {
        Artist artist = UserDatabase.getInstance().getArtistByName(getOwner());
        if (artist != null) {
            artist.addTie();
            ties++;
        }
    }

    @Override
    public void removeTie() {
        Artist artist = UserDatabase.getInstance().getArtistByName(getOwner());
        if (artist != null) {
            artist.removeTie();
            ties = Math.max(0, ties - 1);
        }
    }
}
