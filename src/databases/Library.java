package databases;

import app.audiofiles.collections.Album;
import fileio.commands.search.SearchCommand;
import lombok.Getter;
import app.audiofiles.collections.Playlist;
import app.audiofiles.collections.Podcast;
import app.audiofiles.Song;
import app.utils.constants.searchbar.SearchBarConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The app library. Used to store all app data (users, songs, playlists etc.)
 */
public final class Library {
    /* Thread-safe singleton */
    @Getter
    private static Library instance;

    static {
        synchronized (Library.class) {
            instance = new Library();
        }
    }

    /* Collections */
    private final List<Song> songs = new ArrayList<>();
    @Getter
    private final List<Podcast> podcasts = new ArrayList<>();
    private final List<Playlist> playlists = new ArrayList<>();
    private final List<Album> albums = new ArrayList<>();

    private Library() {
    }

    /**
     * Clears all library data.
     * <p>
     * !!! This method should be removed in production !!!
     */
    public void clear() {
        songs.clear();
        podcasts.clear();
        playlists.clear();
        albums.clear();
    }

    /**
     * @param genre the song genre
     * @return All songs with the given genre
     */
    public List<Song> getSongsByGenre(final String genre) {
        List<Song> result = new ArrayList<>(songs);
        result.removeIf(song -> !song.getGenre().equalsIgnoreCase(genre));
        return result;
    }

    /**
     * @param albumName the album name
     * @return The found album or null
     */
    public Album getAlbumByName(final String albumName) {
        return albums.stream().filter(album -> album.getName().equals(albumName))
                .findAny().orElse(null);
    }

    /**
     * @return The top 5 songs (by likes)
     */
    public List<String> getTop5Songs() {
        List<Song> sortedSongs = new ArrayList<>(songs);
        sortedSongs.sort(Comparator.comparing(Song::getLikes).reversed());

        return sortedSongs.subList(
                0, Math.min(songs.size(), SearchBarConstants.RESULTS_MAX_SIZE)
        ).stream().map(Song::getName).toList();
    }

    /**
     * @return The top 5 playlists (by follows)
     */
    public List<String> getTop5Playlists() {
        List<Playlist> sortedPlaylists = new ArrayList<>(playlists);
        sortedPlaylists.sort(Comparator.comparing(Playlist::getFollowers).reversed());

        return sortedPlaylists.subList(
                0, Math.min(playlists.size(), SearchBarConstants.RESULTS_MAX_SIZE)
        ).stream().map(Playlist::getName).toList();
    }

    /**
     * @return The top 5 albums (by total song likes)
     */
    public List<String> getTop5Albums() {
        List<Album> sortedAlbums = new ArrayList<>(albums);
        sortedAlbums.sort(Comparator.comparing(Album::getName));
        sortedAlbums.sort(Comparator.comparing(Album::getLikes).reversed());

        return sortedAlbums.subList(
                0, Math.min(albums.size(), SearchBarConstants.RESULTS_MAX_SIZE)
        ).stream().map(Album::getName).toList();
    }

    /**
     * @param filter search filter
     * @return The found albums
     */
    public List<Album> getAlbumsByFilters(final SearchCommand.SearchFilter filter) {
        List<Album> results = new ArrayList<>();
        UserDatabase.getInstance().getArtists().forEach(artist ->
                results.addAll(artist.getAlbums())
        );

        if (filter.getOwner() != null) {
            results.removeIf(album -> !album.getOwner().equalsIgnoreCase(filter.getOwner()));
        }

        if (filter.getDescription() != null) {
            results.removeIf(album -> !album.getDescription().toLowerCase()
                    .contains(filter.getDescription().toLowerCase()));
        }

        if (filter.getName() != null) {
            results.removeIf(album -> !album.getName().toLowerCase()
                    .startsWith(filter.getName().toLowerCase()));
        }

        return results;
    }

    /**
     * @param filter search filters
     * @return The found podcasts
     */
    public List<Podcast> getPodcastsByFilters(final SearchCommand.SearchFilter filter) {
        List<Podcast> results = new ArrayList<>(podcasts);

        if (filter.getName() != null) {
            results.removeIf(podcast -> !podcast.getName().toLowerCase()
                    .startsWith(filter.getName().toLowerCase()));
        }

        if (filter.getOwner() != null) {
            results.removeIf(podcast -> !podcast.getOwner().equalsIgnoreCase(filter.getOwner()));
        }

        return results;
    }

    /**
     * @param filter   search filters
     * @param username current user
     * @return The found playlists
     */
    public List<Playlist> getPlaylistsByFilters(final SearchCommand.SearchFilter filter,
                                                final String username) {
        List<Playlist> results = new ArrayList<>(playlists);
        results.removeIf(playlist -> playlist.isPrivate() && !playlist.getOwner().equals(username));

        if (filter.getOwner() != null) {
            results.removeIf(playlist -> !playlist.getOwner().equalsIgnoreCase(filter.getOwner()));
        }

        if (filter.getName() != null) {
            results.removeIf(playlist -> !playlist.getName().toLowerCase()
                    .startsWith(filter.getName().toLowerCase()));
        }

        return results;
    }

    /**
     * @param filter search filters
     * @return The found songs
     */
    public List<Song> getSongsByFilters(final SearchCommand.SearchFilter filter) {
        List<Song> results = new ArrayList<>(songs);

        if (filter.getName() != null) {
            results.removeIf(song -> !song.getName().toLowerCase()
                    .startsWith(filter.getName().toLowerCase()));
        }

        if (filter.getAlbum() != null) {
            results.removeIf(song -> !song.getAlbum().toLowerCase()
                    .startsWith(filter.getAlbum().toLowerCase()));
        }

        if (filter.getTags() != null) {
            results.removeIf(song -> !song.getTags().containsAll(filter.getTags()));
        }

        if (filter.getLyrics() != null) {
            results.removeIf(song ->
                    !song.getLyrics().toLowerCase().toLowerCase()
                            .contains(filter.getLyrics().toLowerCase()));
        }

        if (filter.getGenre() != null) {
            results.removeIf(song -> !song.getGenre().equalsIgnoreCase(filter.getGenre()));
        }

        if (filter.getReleaseYear() != null) {
            /* Negating by changing the sign of the equation */
            int sign = (filter.getReleaseYear().charAt(0) == '<') ? 1 : -1;
            results.removeIf(song -> song.getReleaseYear() * sign > filter.getYearAsInt() * sign);
        }

        if (filter.getArtist() != null) {
            results.removeIf(song -> !song.getArtist().equalsIgnoreCase(filter.getArtist()));
        }

        return results;
    }

    /**
     * @param song playlist to be added
     */
    public void addSong(final Song song) {
        songs.add(song);
    }

    /**
     * @param playlist playlist to be added
     */
    public void addPlaylist(final Playlist playlist) {
        playlists.add(playlist);
    }

    /**
     * Completely removes a user's playlists
     *
     * @param removedPlaylists the list of playlists
     */
    public void removePlaylists(final List<Playlist> removedPlaylists) {
        UserDatabase.getInstance().removePlaylists(removedPlaylists);
        playlists.removeAll(removedPlaylists);
    }

    /**
     * @param album album to be added
     */
    public void addAlbum(final Album album) {
        songs.addAll(album.getSongs());
        albums.add(album);
    }

    /**
     * @param album album to be deleted
     */
    public void removeAlbum(final Album album) {
        for (Song song : album.getSongs()) {
            songs.remove(song);
        }

        albums.remove(album);
    }

    /**
     * Completely removes an artist's albums
     *
     * @param removedAlbums the albums
     */
    public void removeAlbums(final List<Album> removedAlbums) {
        for (Album album : removedAlbums) {
            UserDatabase.getInstance().removeSongs(album.getSongs());
            songs.removeAll(album.getSongs());
        }

        albums.removeAll(removedAlbums);
    }

    /**
     * @param podcast podcast to be added
     */
    public void addPodcast(final Podcast podcast) {
        podcasts.add(podcast);
    }

    /**
     * @param podcast podcast to be deleted
     */
    public void removePodcast(final Podcast podcast) {
        podcasts.remove(podcast);
    }

    /**
     * Completely removes the podcasts of a host
     *
     * @param removedPodcasts the podcasts
     */
    public void removePodcasts(final List<Podcast> removedPodcasts) {
        podcasts.removeAll(removedPodcasts);
    }
}
