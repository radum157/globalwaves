package app.users.normal;

import app.audiofiles.Song;
import app.audiofiles.collections.Playlist;
import app.page.Page;
import app.page.creator.ArtistPage;
import app.page.user.HomePage;
import app.page.user.LikedContentPage;
import app.users.tie.TiedUserEntity;
import app.utils.constants.page.PageConstants;
import app.utils.page.PageInfo;
import app.utils.page.data.Merchandise;
import fileio.input.UserInput;
import lombok.Getter;
import observer.Observer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Observer user class. Can subscribe to an artist / host's page and receive notifications.
 * All user page data is stored and handled here.
 */
public abstract class ObserverUser extends TiedUserEntity implements Observer {
    private final List<String> notifications = new LinkedList<>();

    /* Page navigation */
    private List<Page> pageHistory = new ArrayList<>();
    private int historyIndex;

    @Getter
    protected final List<Song> likedSongs = new ArrayList<>();
    @Getter
    protected final List<Playlist> followedPlaylists = new ArrayList<>();

    @Getter
    private Page currentPage;
    @Getter
    private final HomePage homePage;
    @Getter
    private final LikedContentPage likedContentPage;

    @Getter
    private final List<Merchandise> boughtMerch = new ArrayList<>();

    public ObserverUser(final UserInput userInput) {
        super(userInput);

        homePage = new HomePage(PageInfo.PageType.HOME, likedSongs, followedPlaylists);
        likedContentPage = new LikedContentPage(
                PageInfo.PageType.LIKED_CONTENT, likedSongs, followedPlaylists
        );

        currentPage = homePage;
    }

    /**
     * Sets the current page and updates the history
     *
     * @param page the new page
     */
    public void setCurrentPage(final Page page) {
        if (historyIndex < pageHistory.size() - 1) {
            pageHistory = pageHistory.subList(0, historyIndex);
        }

        pageHistory.add(page);
        historyIndex = pageHistory.size() - 1;

        currentPage.removeTie();
        currentPage = page;
        currentPage.addTie();
    }

    /**
     * Moves to the next visited page
     *
     * @return Success status
     */
    public boolean getNextPage() {
        if (historyIndex >= pageHistory.size() - 1) {
            return false;
        }

        historyIndex++;
        currentPage.removeTie();
        currentPage = pageHistory.get(historyIndex);
        currentPage.addTie();

        return true;
    }

    /**
     * Moves to the previously visited page
     *
     * @return Success status
     */
    public boolean getPreviousPage() {
        if (historyIndex <= 0) {
            return false;
        }

        historyIndex--;
        currentPage.removeTie();
        currentPage = pageHistory.get(historyIndex);
        currentPage.addTie();

        return true;
    }

    /**
     * Copies and clears the notifications list
     *
     * @return The copy of the list
     */
    public List<String> popNotifications() {
        List<String> notificationCopies = new ArrayList<>(notifications);
        notifications.clear();
        return notificationCopies;
    }

    /**
     * Buys the given merch from the current page
     *
     * @param merchName the merch name
     * @return Success status message
     */
    public String buyMerch(final String merchName) {
        if (currentPage.getPageInfo().getPageType() != PageInfo.PageType.ARTIST) {
            return PageConstants.BUY_PAGE_INVALID;
        }

        Merchandise merch = ((ArtistPage) currentPage).getMerchByName(merchName);
        if (merch == null) {
            return "The merch " + merchName + " doesn't exist.";
        }

        boughtMerch.add(merch);
        merch.buy();

        return getName() + PageConstants.MERCH_BUY_SUCCESS;
    }

    @Override
    public final void notify(final String notification) {
        notifications.add(notification);
    }
}
