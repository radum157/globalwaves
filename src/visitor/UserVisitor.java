package visitor;

import app.users.normal.User;
import app.users.creators.Artist;
import app.users.creators.Host;

/**
 * Visitor for all 3 user types. Used to gather info about the user, ergo void
 */
public interface UserVisitor {
    /**
     * Visit a normal user
     *
     * @param user the user
     */
    void visit(User user);

    /**
     * Visit a host
     *
     * @param host the host
     */
    void visit(Host host);

    /**
     * Visit an artist
     *
     * @param artist the artist
     */
    void visit(Artist artist);
}
