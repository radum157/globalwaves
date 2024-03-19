package fileio.commands.utils.formats;

import lombok.Getter;
import app.audiofiles.collections.Playlist;
import app.audiofiles.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the format playlists should be printed to JSON in
 */
@Getter
public final class PlaylistFormat {
    private final String name;
    private final List<String> songs = new ArrayList<>();
    private final String visibility;
    private final int followers;

    public PlaylistFormat(final Playlist playlist) {
        name = playlist.getName();
        visibility = (playlist.isPrivate()) ? "private" : "public";
        followers = playlist.getFollowers();

        for (Song song : playlist.getSongs()) {
            songs.add(song.getName());
        }
    }

    /**
     * @param playlists the list to format
     * @return The formatted arraylist
     */
    public static List<PlaylistFormat> formatList(final List<Playlist> playlists) {
        List<PlaylistFormat> result = new ArrayList<>(playlists.size());
        for (Playlist playlist : playlists) {
            result.add(new PlaylistFormat(playlist));
        }

        return result;
    }
}
