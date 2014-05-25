package com.agcy.vkproject.spy.Fragments;

import android.util.Log;
import android.widget.AbsListView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Fragments.Interfaces.LoadImagesOnScrollListener;

/**
 * Created by kiolt_000 on 24-May-14.
 */
public abstract class UpdatesFragment extends ListFragment {

    @Override
    public void onPause() {
        super.onPause();
        Log.i("AGCY SPY", "OnlinesFragment paused");
        if(adapter!=null){
            ((UpdatesAdapter)adapter).pauseNew();
            ((UpdatesAdapter)adapter).recreateHeaders();
        }

    }

    @Override
    public AbsListView.OnScrollListener getScrollListener() {
        return new LoadImagesOnScrollListener(getListView()){
            @Override
            public int getOffset() {

                int offset = 0;
                if (firstVisibleItem == 0 && hasHeader()) {
                    visibleItemCount--;
                } else {
                    if (firstVisibleItem != 0 && hasHeader()) {
                        offset = 1;
                    }
                }
                return offset;
            }
        };
    }

    @Override
    protected void bindContent() {
        super.bindContent();
        if(adapter == null || adapter.isEmpty()){
            bindContentListener();
        }else{
            bindNewItemListener();
        }
        bindGlobalListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("AGCY SPY","OnlinesFragment resumed");
        if(adapter!=null){
            ((UpdatesAdapter)adapter).resumeNew();
        }
    }

    public void recreateHeaders(){

        if(adapter!=null)
            ((UpdatesAdapter)adapter).recreateHeaders();
    }



    public abstract void bindContentListener();
    public abstract void bindGlobalListener();
    public abstract void bindNewItemListener();


}