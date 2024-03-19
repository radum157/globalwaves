package visitor;

/**
 * Visitable user objects
 */
public interface VisitableUser {
    /**
     * Accept a UserVisitor
     *
     * @param visitor the visitor
     */
    void accept(UserVisitor visitor);
}
