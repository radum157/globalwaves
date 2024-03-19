package app.searchbar;

import app.admin.Admin;
import app.page.handler.PageHandler;
import databases.UserDatabase;
import app.users.creators.ContentCreator;
import lombok.Setter;
import app.audiofiles.AudioFile;
import app.audiofiles.collections.Playlist;
import fileio.commands.search.SearchCommand;
import fileio.commands.search.SearchResponse;
import databases.Library;
import app.users.normal.User;
import app.utils.constants.audio.PlaylistConstants;
import app.utils.constants.searchbar.SearchBarConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Searchbar class. Each user has an independent searchbar.
 */
public final class SearchBar {
    private List<AudioFile> foundFiles;
    private AudioFile selectedFile;
    private List<ContentCreator> foundCreators;

    private boolean syncState;

    @Setter
    private boolean isOffline;

    public SearchBar() {
    }

    /**
     * Retrieves and removes the current selection
     *
     * @return The selection
     */
    public AudioFile popSelection() {
        AudioFile copy = selectedFile;
        selectedFile = null;
        return copy;
    }

    /**
     * @return If the state should be saved
     */
    public boolean syncState() {
        return syncState;
    }

    /**
     * Sets all previous data to null
     */
    private void prepareForSearch() {
        selectedFile = null;
        foundFiles = null;
        foundCreators = null;
        syncState = false;
    }

    /**
     * @param creators the found content creators
     * @return An arraylist containing only the names of the found content creators
     */
    private List<String> parseCreatorResults(final List<ContentCreator> creators) {
        foundCreators = new ArrayList<>(creators.subList(
                0, Math.min(SearchBarConstants.RESULTS_MAX_SIZE, creators.size())
        ));

        return foundCreators.stream().map(ContentCreator::getName).toList();
    }

    /**
     * @param files the found audio files
     * @return An arraylist only containing the name of the found files
     */
    private List<String> parseFileResults(final List<? extends AudioFile> files) {
        foundFiles = new ArrayList<>(files.subList(
                0, Math.min(SearchBarConstants.RESULTS_MAX_SIZE, files.size())
        ));

        return foundFiles.stream().map(AudioFile::getName).toList();
    }

    /**
     * Performs a search operation on the library
     *
     * @param command the input command
     * @param library the library instance
     * @param user    the user
     * @return The corresponding response
     */
    public SearchResponse doSearch(final SearchCommand command, final Library library,
                                   final User user, final UserDatabase userDatabase) {
        prepareForSearch();
        List<String> results = switch (command.getType()) {
            case "song" -> parseFileResults(library.getSongsByFilters(command.getFilters()));
            case "podcast" -> parseFileResults(library.getPodcastsByFilters(command.getFilters()));
            case "playlist" -> parseFileResults(
                    library.getPlaylistsByFilters(command.getFilters(), user.getName())
            );
            case "album" -> parseFileResults(library.getAlbumsByFilters(command.getFilters()));
            case "artist" -> parseCreatorResults(
                    userDatabase.getArtistsByFilters(command.getFilters())
            );
            case "host" -> parseCreatorResults(
                    userDatabase.getHostsByFilters(command.getFilters())
            );
            /* Unknown type */
            default -> null;
        };

        if (command.getType().equals("podcast")) {
            syncState = true;
        }

        user.getMediaPlayer().clearPlayer(command.getTimestamp(), false);

        SearchResponse searchResponse = new SearchResponse(command);
        searchResponse.setResults(results);

        if (isOffline) {
            searchResponse.setMessage(Admin.userOffline(user.getName()));
            searchResponse.getResults().clear();
            prepareForSearch();
        } else {
            searchResponse.setMessage("Search returned " + results.size() + " results");
        }

        return searchResponse;
    }

    /**
     * Selects a found file
     *
     * @param itemNumber the index of the file
     * @param user       the user
     * @return Success status message as defined in SearchBarConstants
     */
    public String doSelect(final int itemNumber, final User user) {
        if (isOffline) {
            return Admin.userOffline(user.getName());
        }

        if (foundFiles == null && foundCreators == null) {
            return SearchBarConstants.SEARCHBAR_NOT_SEARCHED;
        }

        if (foundCreators != null) {
            String message = new PageHandler().visitPage(foundCreators, itemNumber, user);
            foundCreators = null;
            return message;
        }

        if (itemNumber > foundFiles.size()) {
            return SearchBarConstants.SEARCHBAR_HIGH_ID;
        }

        selectedFile = foundFiles.get(itemNumber - 1);
        foundFiles = null;

        return "Successfully selected " + selectedFile.getName() + ".";
    }

    /**
     * Follows the selection
     *
     * @param user the user
     * @return Success status message
     */
    public String followPlaylist(final User user) {
        if (isOffline) {
            return Admin.userOffline(user.getName());
        }

        if (selectedFile == null) {
            return PlaylistConstants.FOLLOW_NO_SOURCE;
        }

        if (user.getPlaylists().contains(selectedFile)) {
            return PlaylistConstants.FOLLOW_OWN_ERROR;
        }

        if (user.getFollowedPlaylists().contains(selectedFile)) {
            user.getFollowedPlaylists().remove((Playlist) selectedFile);
            return selectedFile.removeFollow();
        }

        String message = selectedFile.addFollow();
        if (!message.equals(PlaylistConstants.FOLLOW_NOT_PLAYLIST)) {
            user.getFollowedPlaylists().add((Playlist) selectedFile);
        }

        return message;
    }
}
