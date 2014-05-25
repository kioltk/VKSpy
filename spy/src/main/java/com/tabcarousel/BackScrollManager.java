

package com.tabcarousel;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Handles scrolling back of a list tied to a header.
 * <p>
 * This is used to implement a header that scrolls up with the content of a list
 * to be partially obscured.
 */
public class BackScrollManager implements OnScrollListener {

    private final CarouselContainer mCarousel;
    private final int currentTab;

    /**
     * @param carouselHeader The {@link CarouselContainer} to move
     */
    public BackScrollManager(CarouselContainer carouselHeader, int currentTab) {
        // Initialize the header
        mCarousel = carouselHeader;
        this.currentTab = currentTab;
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        // Don't move the carousel if: 1) It is already being animated
        if (mCarousel == null || mCarousel.isTabCarouselIsAnimating()) {
            return;
        }

        // If the FIRST item is not visible on the screen, then the carousel
        // must be pinned
        // at the top of the screen.
        if (firstVisibleItem != 0) {
            mCarousel.moveToTop(currentTab);
            return;
        }

        final View topView = view.getChildAt(firstVisibleItem);
        if (topView == null) {
            return;
        }

        final float y = view.getChildAt(firstVisibleItem).getTop();
        final float amtToScroll = Math.max(y, -mCarousel.getAllowedVerticalScrollLength());
        mCarousel.moveToYCoordinate(amtToScroll,currentTab);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

}
