package app.users.creators;

import app.audiofiles.collections.Album;
import app.users.normal.User;
import app.utils.constants.searchbar.SearchBarConstants;
import app.utils.constants.users.ArtistConstants;
import databases.Library;
import app.page.Page;
import app.page.creator.ArtistPage;
import app.utils.page.PageInfo;
import databases.UserDatabase;
import fileio.input.UserInput;
import lombok.Getter;
import visitor.UserVisitor;
import visitor.VisitableUser;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;

@Getter
public final class Artist extends ContentCreator implements VisitableUser {
    private final ArtistPage artistPage;
    private final List<Album> albums = new ArrayList<>();

    private double merchRevenue;

    public Artist(final UserInput userInput) {
        super(userInput);
        artistPage = new ArtistPage(new PageInfo(this, PageInfo.PageType.ARTIST), albums);
    }

    /**
     * Adds income from merch
     *
     * @param income the merch price
     */
    public void addMerchRevenue(final double income) {
        merchRevenue += income;
    }

    @Override
    public void accept(final UserVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void untieFrom(final Library library) {
        library.removeAlbums(albums);
    }

    /**
     * @param albumName the album name
     * @return The found album or null
     */
    public Album findAlbum(final String albumName) {
        return albums.stream().filter(album -> album.getName().equals(albumName))
                .findAny().orElse(null);
    }

    /**
     * @param userDatabase the user database
     * @return The top 5 fans by the listen count
     */
    public List<User> getTop5Fans(final UserDatabase userDatabase) {
        Map<User, Integer> fans = new LinkedHashMap<>();

        for (User user : userDatabase.getUsers()) {
            user.getListenedSongs().forEach((key, value) -> {
                if (key.getArtist().equals(getName())) {
                    fans.merge(user, value, Integer::sum);
                }
            });
        }

        List<Map.Entry<User, Integer>> entries = new ArrayList<>(fans.entrySet());
        entries.sort(Map.Entry.comparingByKey(Comparator.comparing(User::getName)));
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        entries = entries.subList(0, Math.min(entries.size(), SearchBarConstants.RESULTS_MAX_SIZE));

        Map<User, Integer> topFans = new LinkedHashMap<>();
        entries.forEach(entry -> topFans.put(entry.getKey(), entry.getValue()));

        return new ArrayList<>(topFans.keySet());
    }

    /**
     * Adds a new album
     *
     * @param album the album
     * @return Success status message
     */
    public String addAlbum(final Album album) {
        /* Check for duplicates. Equivalent to double for */
        if (album.getSongs().stream().anyMatch(song ->
                album.getSongs().stream().anyMatch(song2 ->
                        !song2.equals(song) && song2.getName().equals(song.getName())))) {
            return getName() + ArtistConstants.DUPLICATE_SONG;
        }

        Library.getInstance().addAlbum(album);
        albums.add(album);
        notifyAll("Album");

        return getName() + ArtistConstants.ADD_SUCCESS;
    }

    /**
     * Removes an existing album
     *
     * @param albumName the album name
     * @return Success status message
     */
    public String removeAlbum(final String albumName) {
        Album album = findAlbum(albumName);
        if (album == null) {
            return getName() + ArtistConstants.NO_ALBUM;
        }

        if (album.getTies() > 0) {
            return getName() + ArtistConstants.DELETE_FAIL;
        }

        Library.getInstance().removeAlbum(album);
        albums.remove(album);
        return getName() + ArtistConstants.DELETE_SUCCESS;
    }

    public int getLikes() {
        return albums.stream().mapToInt(Album::getLikes).sum();
    }

    @Override
    public Page getPage() {
        return artistPage;
    }
}
