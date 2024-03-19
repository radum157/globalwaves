package app.page;

import app.utils.tie.TiedEntity;
import lombok.Getter;
import app.utils.page.PageInfo;

/**
 * Generic page class
 */
@Getter
public abstract class Page implements TiedEntity {
    private final PageInfo pageInfo;

    protected Page(final PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /**
     * Adds a tie to the creator
     */
    @Override
    public final void addTie() {
        if (pageInfo.getCreator() != null) {
            pageInfo.getCreator().addTie();
        }
    }

    /**
     * Removes an existing tie to the creator
     */
    @Override
    public final void removeTie() {
        if (pageInfo.getCreator() != null) {
            pageInfo.getCreator().removeTie();
        }
    }

    /**
     * @return The page content
     */
    public abstract String printPage();
}
