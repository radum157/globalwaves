package fileio.commands.utils.formats;

import app.audiofiles.Song;
import app.audiofiles.collections.Album;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class AlbumFormat {
    private final String name;
    private final List<String> songs = new ArrayList<>();

    public AlbumFormat(final Album album) {
        name = album.getName();

        for (Song song : album.getSongs()) {
            songs.add(song.getName());
        }
    }

    /**
     * Formats a given album arraylist
     *
     * @param albums the album list
     * @return The formatted list
     */
    public static List<AlbumFormat> formatList(final List<Album> albums) {
        List<AlbumFormat> result = new ArrayList<>();
        for (Album album : albums) {
            result.add(new AlbumFormat(album));
        }

        return result;
    }
}
