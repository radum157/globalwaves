package app.page.user;

import app.audiofiles.Song;
import app.audiofiles.collections.Playlist;
import app.utils.constants.searchbar.SearchBarConstants;
import app.utils.page.PageInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Home page class
 */
public final class HomePage extends LikedContentPage {
    private final List<Song> songRecommendations = new ArrayList<>();
    private final List<Playlist> playlistRecommendations = new ArrayList<>();

    public HomePage(final PageInfo.PageType pageType, final List<Song> likedSongs,
                    final List<Playlist> followedPlaylists) {
        super(pageType, likedSongs, followedPlaylists);
    }

    /**
     * Adds the given song to the recommendations
     *
     * @param song the song
     */
    public void recommendSong(final Song song) {
        songRecommendations.add(song);
    }

    /**
     * Adds the given playlist to the recommendations
     *
     * @param playlist the playlist
     */
    public void recommendPlaylist(final Playlist playlist) {
        playlistRecommendations.add(playlist);
    }

    @Override
    protected void parseSongs(final StringBuilder content) {
        List<Song> sortedSongs = new ArrayList<>(likedSongs);
        sortedSongs.sort(Comparator.comparing(Song::getLikes).reversed());
        sortedSongs = sortedSongs.subList(
                0, Math.min(SearchBarConstants.RESULTS_MAX_SIZE, sortedSongs.size())
        );

        for (Song song : sortedSongs) {
            content.append(song.getName()).append(", ");
        }
    }

    @Override
    protected void parsePlaylists(final StringBuilder content) {
        List<Playlist> sortedPlaylists = new ArrayList<>(followedPlaylists);
        sortedPlaylists.sort(Comparator.comparing(Playlist::getLikes).reversed());
        sortedPlaylists = sortedPlaylists.subList(
                0, Math.min(SearchBarConstants.RESULTS_MAX_SIZE, sortedPlaylists.size())
        );

        for (Playlist playlist : sortedPlaylists) {
            content.append(playlist.getName()).append(", ");
        }
    }

    /**
     * Goes through the song recommendations and appends them to the page content
     *
     * @param content the page content
     */
    private void parseSongRecommendations(final StringBuilder content) {
        content.append("Song recommendations:\n\t[");
        songRecommendations.forEach(song -> content.append(song.getName()).append(", "));
    }

    /**
     * Goes through the playlist recommendations and appends them to the page content
     *
     * @param content the page content
     */
    private void parsePlaylistRecommendations(final StringBuilder content) {
        content.append("Playlists recommendations:\n\t[");
        playlistRecommendations.forEach(playlist ->
                content.append(playlist.getName()).append(", ")
        );
    }

    @Override
    public String printPage() {
        StringBuilder content = new StringBuilder(super.printPage());
        content.append("\n\n");

        parseSongRecommendations(content);

        if (!songRecommendations.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]\n\n");

        parsePlaylistRecommendations(content);

        if (!playlistRecommendations.isEmpty()) {
            content.setLength(content.length() - 2);
        }
        content.append("]");

        return content.toString();
    }
}
