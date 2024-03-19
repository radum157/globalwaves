package app.users.normal;

import app.audiofiles.collections.Podcast;
import app.users.creators.Artist;
import app.utils.audiostate.AudioFileState;
import app.utils.constants.searchbar.SearchBarConstants;
import databases.UserDatabase;
import fileio.input.EpisodeInput;
import fileio.input.UserInput;
import lombok.Getter;
import app.audiofiles.AudioFile;
import app.audiofiles.collections.Playlist;
import app.audiofiles.Song;
import databases.Library;
import app.mediaplayer.MediaPlayer;
import app.searchbar.SearchBar;
import app.utils.constants.audio.PlaylistConstants;
import app.utils.constants.users.UserConstants;
import visitor.UserVisitor;
import visitor.VisitableUser;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Random;
import java.util.LinkedHashMap;

/**
 * Normal user class
 */
@Getter
public final class User extends ObserverUser implements VisitableUser {
    /* User-specific entities */
    private final SearchBar searchBar = new SearchBar();
    private final MediaPlayer mediaPlayer;

    private final List<Playlist> playlists = new ArrayList<>();

    /* For statistics */
    private final Map<Song, Integer> listenedSongs = new HashMap<>();
    private final Map<EpisodeInput, Integer> listenedEpisodes = new HashMap<>();

    /* Note that listenedSongs != premiumSongs + freeSongs */
    private final Map<Song, Integer> premiumSongs = new HashMap<>();
    private final Map<Song, Integer> freeSongs = new HashMap<>();

    private AudioFile lastRecommendation;

    private boolean premium;
    private boolean connected = true;

    public User(final UserInput userInput) {
        super(userInput);
        this.mediaPlayer = new MediaPlayer(this);
    }

    /**
     * Updates recommendations by adding a random song
     *
     * @param library the library instance
     * @return Success status message
     */
    public String updateRandomSong(final Library library) {
        AudioFileState fileState = mediaPlayer.getFileState();
        Song song = (Song) mediaPlayer.getAudioFile();

        if (fileState.getTimePosition() < UserConstants.RECOMMENDATION_MIN_TIME) {
            return UserConstants.RECOMMENDATION_FAIL;
        }

        List<Song> recommendations = library.getSongsByGenre(song.getGenre());
        if (recommendations.isEmpty()) {
            return UserConstants.RECOMMENDATION_FAIL;
        }

        int index = new Random(fileState.getTimePosition()).nextInt(recommendations.size());
        getHomePage().recommendSong(recommendations.get(index));
        lastRecommendation = recommendations.get(index);

        return "The recommendations for user " + getName() + " have been updated successfully.";
    }

    /**
     * Helper function. Adds count distinct songs to target.
     *
     * @param target the target list
     * @param source the song source
     * @param count  the count
     */
    private void addToList(final List<Song> target, final List<Song> source, final int count) {
        int addedSongs = 0;
        for (Song song : source) {
            if (target.contains(song)) {
                continue;
            }

            target.add(song);
            addedSongs++;

            if (count == addedSongs) {
                break;
            }
        }
    }

    /**
     * Loads the last received recommendation
     *
     * @param timestamp the timestamp
     * @return An error or what MediaPlayer.loadFile returns
     */
    public String loadRecommendation(final int timestamp) {
        if (lastRecommendation == null) {
            return UserConstants.LOAD_FAIL;
        }
        return mediaPlayer.loadFile(lastRecommendation, false, timestamp);
    }

    /**
     * Searches for genres in likedSongs, playlists and followedPlaylists
     *
     * @return The top 3 genres
     */
    private List<String> getTop3Genres() {
        Map<String, Integer> genres = new LinkedHashMap<>();

        likedSongs.forEach(song -> genres.merge(song.getGenre(), 1, Integer::sum));
        for (Playlist playlist : playlists) {
            playlist.getSongs().forEach(song -> genres.merge(song.getGenre(), 1, Integer::sum));
        }
        for (Playlist playlist : followedPlaylists) {
            playlist.getSongs().forEach(song -> genres.merge(song.getGenre(), 1, Integer::sum));
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(genres.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        entries = entries.subList(
                0, Math.min(UserConstants.RANDOM_PLAYLIST_GENRES, entries.size())
        );

        Map<String, Integer> topGenreMap = new LinkedHashMap<>();
        entries.forEach(entry -> topGenreMap.put(entry.getKey(), entry.getValue()));

        return new ArrayList<>(topGenreMap.keySet());
    }

    /**
     * Adds a random playlist to recommendations
     *
     * @param library the library instance
     * @return Success status message
     */
    public String updateRandomPlaylist(final Library library) {
        List<String> topGenres = getTop3Genres();
        if (topGenres.isEmpty()) {
            return UserConstants.RECOMMENDATION_FAIL;
        }

        List<Song> recommendations = new ArrayList<>();

        for (int i = 0; i < topGenres.size(); i++) {
            List<Song> foundSongs = library.getSongsByGenre(topGenres.get(i));
            foundSongs.sort(Comparator.comparing(Song::getLikes));
            addToList(recommendations, foundSongs, UserConstants.GENRE_COUNT[i]);
        }

        Playlist recommendation = new Playlist(getName() + "'s recommendations", getName(), true);
        recommendation.getSongs().addAll(recommendations);

        getHomePage().recommendPlaylist(recommendation);
        lastRecommendation = recommendation;

        return "The recommendations for user " + getName() + " have been updated successfully.";
    }

    /**
     * Adds a fans' playlist to recommendations
     *
     * @param timestamp    the timestamp
     * @param userDatabase the user database
     * @return Success status message
     */
    public String updateFansPlaylist(final int timestamp, final UserDatabase userDatabase) {
        userDatabase.updateAllPlayers(timestamp);

        Song song = (Song) mediaPlayer.getAudioFile();
        Artist artist = userDatabase.getArtistByName(song.getArtist());
        List<User> topFans = artist.getTop5Fans(userDatabase);

        Playlist recommendation = new Playlist(
                artist.getName() + " Fan Club recommendations",
                getName(),
                true
        );

        for (User user : topFans) {
            List<Song> likedSongs = new ArrayList<>(user.getLikedSongs());
            likedSongs.sort(Comparator.comparing(Song::getLikes));
            likedSongs = likedSongs.subList(
                    0, Math.min(SearchBarConstants.RESULTS_MAX_SIZE, likedSongs.size())
            );

            likedSongs.forEach(likedSong -> {
                if (!recommendation.getSongs().contains(likedSong)) {
                    recommendation.addSong(likedSong);
                }
            });
        }

        if (recommendation.getSongs().isEmpty()) {
            return UserConstants.RECOMMENDATION_FAIL;
        }

        getHomePage().recommendPlaylist(recommendation);
        lastRecommendation = recommendation;

        return "The recommendations for user " + getName() + " have been updated successfully.";
    }

    /**
     * Adds the revenue for listened songs while on premium.
     * Clears the premium listened songs map.
     */
    public void addPremiumRevenue() {
        int size = premiumSongs.values().stream().mapToInt(Integer::intValue).sum();

        premiumSongs.forEach((key, value) ->
                key.addRevenue((((double) value) * UserConstants.PREM_COST) / ((double) size))
        );
        premiumSongs.clear();
    }

    /**
     * Adds revenue for ads. Clears the free listened songs map.
     *
     * @param adPrice the ad price
     */
    public void addAdRevenue(final double adPrice) {
        int size = freeSongs.values().stream().mapToInt(Integer::intValue).sum();

        freeSongs.forEach((key, value) ->
                key.addRevenue((((double) value) * adPrice) / ((double) size))
        );
        freeSongs.clear();
    }

    /**
     * Adds a song to a map using merge with Integer::sum
     *
     * @param song    the song
     * @param songMap the song-integer map
     * @param count   the count to be added with merge
     */
    private void addSongToMap(final Song song, final Map<Song, Integer> songMap, final int count) {
        if (song != null) {
            songMap.merge(song, count, Integer::sum);
        }
    }

    /**
     * Switches the premium status of a user
     *
     * @return Success status message
     */
    public String switchPremium() {
        if (premium) {
            removeTie();
            addPremiumRevenue();
        } else {
            addTie();
        }

        premium = !premium;
        return getName() + ((premium) ? UserConstants.PREM_BUY : UserConstants.PREM_CANCEL);
    }

    /**
     * Adds an episode at a given index to the list of listened episodes
     *
     * @param podcast the podcast
     * @param index   the index
     * @param count   listen count
     */
    public void listenTo(final Podcast podcast, final int index, final int count) {
        listenedEpisodes.merge(podcast.getEpisodes().get(index), count, Integer::sum);
    }

    /**
     * Adds a song to the list of listened songs
     *
     * @param song  the song
     * @param count listen count
     */
    public void listenTo(final Song song, final int count) {
        if (premium) {
            addSongToMap(song, premiumSongs, count);
        } else {
            addSongToMap(song, freeSongs, count);
        }

        addSongToMap(song, listenedSongs, count);
    }

    @Override
    public void accept(final UserVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void untieFrom(final Library library) {
        followedPlaylists.forEach(Playlist::removeFollow);
        likedSongs.forEach(Song::removeLike);
        library.removePlaylists(playlists);
    }

    /**
     * Creates a new playlist and updates the library
     *
     * @param playlistName the name
     * @param library      the library instance
     * @return Success status message
     */
    public String createPlaylist(final String playlistName, final Library library) {
        if (!connected) {
            return getName() + " is offline.";
        }

        if (getPlaylistByName(playlistName) != null) {
            return PlaylistConstants.PLAYLIST_EXISTS;
        }

        playlists.add(new Playlist(playlistName, getName(), false));
        library.addPlaylist(playlists.get(playlists.size() - 1));

        return PlaylistConstants.CREATE_SUCCESS;
    }

    /**
     * Changes playlist visibility
     *
     * @param playlistId the id of the playlist
     * @return Success status message
     */
    public String switchPlaylistPrivacy(final int playlistId) {
        if (!connected) {
            return getName() + " is offline.";
        }

        Playlist playlist = getPlaylistById(playlistId - 1);
        if (playlist == null) {
            return UserConstants.PLAYLIST_ID_TOO_HIGH;
        }

        playlist.switchPrivacy();
        return PlaylistConstants.UPDATE_PRIVACY
                + ((playlist.isPrivate()) ? "private" : "public") + ".";
    }

    /**
     * Switches the connection status for the user as well as his player and searchbar
     *
     * @param timestamp the timestamp
     */
    public void switchConnectionStatus(final int timestamp) {
        connected = !connected;
        mediaPlayer.setOffline(!connected, timestamp);
        searchBar.setOffline(!connected);
    }

    /**
     * @param name the name of the playlist
     * @return The searched playlist
     */
    public Playlist getPlaylistByName(final String name) {
        return playlists.stream().filter(playlist -> playlist.getName().equals(name))
                .findAny()
                .orElse(null);
    }

    /**
     * @param id the id of the playlist
     * @return The searched playlist
     */
    public Playlist getPlaylistById(final int id) {
        return (id < playlists.size()) ? playlists.get(id) : null;
    }

    /**
     * Adds/removes the given file in the given playlist
     *
     * @param file       the audio file
     * @param playlistId the playlist id
     * @return Success status message as defined in PlaylistConstants
     */
    public String addRemoveInPlaylist(final AudioFile file, final int playlistId) {
        Playlist playlist = getPlaylistById(playlistId - 1);
        if (playlist == null) {
            return PlaylistConstants.ADD_REMOVE_NO_PLAYLIST;
        }

        Song song = (Song) file;
        if (playlist.getSongs().contains(song)) {
            playlist.removeSong(song);
            return PlaylistConstants.REMOVE_SUCCESS;
        }

        playlist.addSong(song);
        return PlaylistConstants.ADD_SUCCESS;
    }

    /**
     * Adds / Removes the given file in the liked songs list
     *
     * @param file the audio file
     * @return Success status message
     */
    public String likeSong(final AudioFile file) {
        if (likedSongs.contains(file)) {
            likedSongs.remove((Song) file);
            return file.removeLike();
        }

        String message = file.addLike();
        if (!message.equals(UserConstants.LIKE_NOT_SONG)) {
            likedSongs.add((Song) file);
        }

        return message;
    }
}
