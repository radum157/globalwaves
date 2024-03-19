package app.utils.page;

import app.users.creators.ContentCreator;
import lombok.Getter;
import lombok.Setter;

/**
 * Class containing information about a page
 */
@Getter
@Setter
public final class PageInfo {
    public enum PageType {
        HOME, LIKED_CONTENT, ARTIST, HOST
    }

    private PageType pageType;
    private ContentCreator creator;

    public PageInfo(final ContentCreator creator, final PageType pageType) {
        this.creator = creator;
        this.pageType = pageType;
    }
}
