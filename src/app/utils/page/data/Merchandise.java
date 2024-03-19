package app.utils.page.data;

import app.users.creators.Artist;
import fileio.commands.wrapper.CommandWrapper;

/**
 * Merchandise record
 *
 * @param name        the name
 * @param price       the price (integer)
 * @param description the description
 * @param owner       the owner of the merch
 */
public record Merchandise(String name, Integer price, String description, Artist owner) {
    /**
     * Performs one buy request on the given merch
     */
    public void buy() {
        owner.addMerchRevenue(price);
    }

    /**
     * Creates a new merchandise from a JSON input
     *
     * @param command the parameters
     * @param artist  the owner of the merch
     * @return The merchandise
     */
    public static Merchandise parseFromCommand(final CommandWrapper command, final Artist artist) {
        return new Merchandise(
                command.getName(),
                command.getPrice(),
                command.getDescription(),
                artist
        );
    }
}
