package fileio.commands.utils.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * Class used to store and set artist revenue
 */
@Getter
@Setter
public final class ArtistRevenue {
    private double merchRevenue;
    private double songRevenue;
    private int ranking;
    private String mostProfitableSong = "N/A";

    public ArtistRevenue(final double songRevenue, final double merchRevenue, final int ranking) {
        this.songRevenue = songRevenue;
        this.merchRevenue = merchRevenue;
        this.ranking = ranking;
    }

    /**
     * @return The total revenue stored
     */
    @JsonIgnore
    public double getTotalRevenue() {
        return songRevenue + merchRevenue;
    }
}
