package com.agcy.vkproject.spy.Fragments.Interfaces;

import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.CustomItems.Interfaces.LoadableImage;

/**
 * Created by kiolt_000 on 25-May-14.
 */
public class LoadImagesOnScrollListener implements AbsListView.OnScrollListener {
    private final ListView listView;
    protected int visibleItemCount;
        protected int firstVisibleItem;
        public boolean firstLoading = true;

    public LoadImagesOnScrollListener(ListView listView){
        this.listView = listView;

    }
    ListAdapter getAdapter(){

        return listView.getAdapter();
    }
    public int getOffset(){
        return 0;
    };
    void loadImages() {

        int offset = getOffset();
        for (int i = firstVisibleItem; i < visibleItemCount + firstVisibleItem; i++) {
            Object item = getAdapter().getItem(i);

            if (item instanceof LoadableImage) {
                LoadableImage updateItem = (LoadableImage) item;
                updateItem.loadImage(listView.getChildAt(i - firstVisibleItem+offset));
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            loadImages();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;

        if (firstLoading && visibleItemCount != 0) {
            firstLoading = false;
            loadImages();
        }

    }
}
