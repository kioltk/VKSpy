package com.agcy.vkproject.spy.Fragments;

import android.util.Log;
import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Models.Update;


public class OnlinesFragment extends ListFragment {

    private UpdatesAdapter.NewItemListener fillContentListener = new UpdatesAdapter.NewItemListener() {
        @Override
        public void newItem(Update item) {
            startLoading();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("AGCY SPY","OnlinesFragment destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("AGCY SPY","OnlinesFragment paused");
        if(adapter!=null){
            ((UpdatesAdapter)adapter).pauseNew();
            ((UpdatesAdapter)adapter).recreateHeaders();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("AGCY SPY","OnlinesFragment resumed");
        if(adapter!=null){
            ((UpdatesAdapter)adapter).resumeNew();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.i("AGCY SPY","OnlinesFragment detached");
    }

    @Override
    public BaseAdapter adapter() {

        UpdatesWithOwnerAdapter adapter =
                new UpdatesWithOwnerAdapter(Helper.convertToStatus(Memory.getOnlines()), context);
        return adapter;

    }

    @Override
    protected void onLoad() {
        super.onLoad();

        if(adapter == null || adapter.isEmpty()){
            Memory.addOnlineOnceListener(this.fillContentListener);
        }else{
            Memory.addOnlineListener(((UpdatesAdapter)adapter).newItemListener);
        }

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if(adapter!=null)
            Memory.removeOnlineListener(((UpdatesAdapter)adapter).newItemListener);

    }

    public void recreateHeaders(){

        if(adapter!=null)
            ((UpdatesAdapter)adapter).recreateHeaders();
    }
}
