package app.page.user;

import app.audiofiles.Song;
import app.audiofiles.collections.Playlist;
import app.page.Page;
import app.utils.page.PageInfo;

import java.util.List;

public class LikedContentPage extends Page {
    protected final List<Song> likedSongs;
    protected final List<Playlist> followedPlaylists;

    public LikedContentPage(final PageInfo.PageType pageType, final List<Song> likedSongs,
                            final List<Playlist> followedPlaylists) {
        super(new PageInfo(null, pageType));
        this.likedSongs = likedSongs;
        this.followedPlaylists = followedPlaylists;
    }

    /**
     * Goes through the liked songs and adds them to the page content string
     *
     * @param content the page content
     */
    protected void parseSongs(final StringBuilder content) {
        for (Song song : likedSongs) {
            content.append(song.getName()).append(" - ").append(song.getArtist()).append(", ");
        }
    }

    /**
     * Goes through the followed playlists and adds them to the page content string
     *
     * @param content the page content
     */
    protected void parsePlaylists(final StringBuilder content) {
        for (Playlist playlist : followedPlaylists) {
            content.append(playlist.getName()).append(" - ")
                    .append(playlist.getOwner()).append(", ");
        }
    }

    /**
     * Only prints liked content page lists
     */
    @Override
    public String printPage() {
        StringBuilder content = new StringBuilder("Liked songs:\n\t[");

        /* Split the content creation for future overriding */
        parseSongs(content);

        if (!likedSongs.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]\n\nFollowed playlists:\n\t[");

        parsePlaylists(content);

        if (!followedPlaylists.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]");

        return content.toString();
    }
}
