package fileio.commands.statistics;

import app.audiofiles.Song;
import app.users.creators.Artist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import databases.Library;
import databases.UserDatabase;
import fileio.commands.Response;
import fileio.commands.utils.constants.ResponseConstants;
import fileio.commands.utils.stats.ArtistRevenue;
import fileio.commands.wrapper.CommandWrapper;
import lombok.Getter;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * End program monetization statistics
 */
public final class EndProgramResponse extends Response {
    private Map<String, ArtistRevenue> result = new LinkedHashMap<>();

    @Getter
    private static Map<Song, Double> songRevenues;

    static {
        synchronized (EndProgramResponse.class) {
            songRevenues = new LinkedHashMap<>();
        }
    }

    public EndProgramResponse(final String command) {
        super(command);
    }

    /**
     * Clears all listened to songs.
     * <p>
     * !!! This method should be removed in production !!!
     */
    public static void clearSongs() {
        songRevenues.clear();
    }

    /**
     * Sorts the result map and sets the ranking.
     */
    private void sortResult() {
        List<Map.Entry<String, ArtistRevenue>> entries = new ArrayList<>(result.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        entries.sort(Map.Entry.comparingByValue(
                Comparator.comparing(ArtistRevenue::getTotalRevenue).reversed()
        ));

        int rank = 1;
        for (var entry : entries) {
            entry.getValue().setRanking(rank++);
        }

        result = new LinkedHashMap<>();
        entries.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    }

    /**
     * Sets the most profitable song for a given artist
     *
     * @param artistName the artist
     * @param target     the entry to be updated
     */
    private void resultSetMostProfitable(final String artistName, final ArtistRevenue target) {
        double maxRevenue = 0.0;
        String songName = "N/A";

        for (var entry : songRevenues.entrySet()) {
            if (!entry.getKey().getArtist().equals(artistName)
                    || entry.getValue() == 0.0) {
                continue;
            }

            if (entry.getValue() > maxRevenue) {
                maxRevenue = entry.getValue();
                songName = entry.getKey().getName();
            } else if (entry.getValue() == maxRevenue
                    && entry.getKey().getName().compareTo(songName) < 0) {
                songName = entry.getKey().getName();
            }
        }

        target.setMostProfitableSong(songName);
    }


    /**
     * Adds all listened to songs in the revenue map
     */
    private void resultAddSongs() {
        for (var entry : songRevenues.entrySet()) {
            ArtistRevenue value = result.get(entry.getKey().getArtist());

            if (value == null) {
                result.put(entry.getKey().getArtist(), new ArtistRevenue(entry.getValue(), 0, 0));
            } else {
                value.setSongRevenue(value.getSongRevenue() + entry.getValue());
            }
        }

        result.forEach(this::resultSetMostProfitable);
    }

    /**
     * Rounds the song revenues to 2 decimals
     */
    private void resultFormatSongs() {
        result.forEach((key, value) ->
                value.setSongRevenue(
                        Math.round(value.getSongRevenue() * ResponseConstants.ROUND_FACTOR)
                                / ResponseConstants.ROUND_FACTOR
                )
        );
    }

    /**
     * Adds all bought merch to the revenue map
     *
     * @param userDatabase the user database
     */
    private void resultAddMerch(final UserDatabase userDatabase) {
        for (Artist artist : userDatabase.getArtists()) {
            if (artist.getMerchRevenue() == 0) {
                continue;
            }

            ArtistRevenue value = result.get(artist.getName());

            if (value == null) {
                result.put(artist.getName(), new ArtistRevenue(0, artist.getMerchRevenue(), 0));
            } else {
                value.setMerchRevenue(artist.getMerchRevenue());
            }
        }
    }

    /**
     * Merges songs with the same name, from the same artist
     */
    public void mergeSongRevenues() {
        Map<Song, Double> mergedMap = new LinkedHashMap<>();
        for (var entry : songRevenues.entrySet()) {
            Song currentSong = entry.getKey();

            /* Check if added */
            if (mergedMap.entrySet().stream().filter(mapEntry ->
                    mapEntry.getKey().getName().equals(currentSong.getName())
                            && mapEntry.getKey().getArtist().equals(currentSong.getArtist())
            ).findAny().orElse(null) != null) {
                continue;
            }

            /* Compute sum */
            double sum = songRevenues.entrySet().stream().filter(mapEntry ->
                    mapEntry.getKey().getName().equals(currentSong.getName())
                            && mapEntry.getKey().getArtist().equals(currentSong.getArtist())
            ).mapToDouble(Map.Entry::getValue).sum();

            mergedMap.put(entry.getKey(), sum);
        }

        songRevenues = mergedMap;
    }


    @Override
    public void getResponse(final Library library, final UserDatabase userDatabase,
                            final CommandWrapper input) {
        /* Use last input to update players */
        userDatabase.updateAllPlayers(input.getTimestamp());
        userDatabase.getUsers().forEach(user -> {
            user.addPremiumRevenue();
            user.addAdRevenue(0.0);
        });

        mergeSongRevenues();

        resultAddSongs();
        resultFormatSongs();
        resultAddMerch(userDatabase);

        sortResult();
    }

    @Override
    public JsonNode toNode() {
        ObjectNode objectNode = (ObjectNode) super.toNode();

        objectNode.putPOJO("result", result);

        return objectNode;
    }
}
