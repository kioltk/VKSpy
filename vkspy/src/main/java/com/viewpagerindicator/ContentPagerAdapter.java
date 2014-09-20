package com.viewpagerindicator;

public interface ContentPagerAdapter<T> {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    // From PagerAdapter
    int getCount();

    T getContent(int position);
}
