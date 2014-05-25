package com.agcy.vkproject.spy.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Listeners.NewUpdateListener;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;


public class OnlinesFragment extends UpdatesFragment {

    private NewUpdateListener contentListener = new NewUpdateListener() {
        @Override
        public void newItem(Update item) {
            createContent();
        }
    };
    private Helper.TrackUpdatedListener trackUpdateListener = new Helper.TrackUpdatedListener() {
        @Override
        public void onUpdate() {
            recreateContent();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.filter_button, (ViewGroup) rootView, true);

        return rootView;
    }



    @Override
    public BaseAdapter adapter() {

        UpdatesWithOwnerAdapter adapter =
                new UpdatesWithOwnerAdapter(Helper.convertToStatus(Memory.getTrackedOnlines()), context);
        return adapter;

    }

    @Override
    public void recreateContent() {

        rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);
        super.recreateContent();
    }

    @Override
    protected void onContentBinded() {
        if (adapter == null || adapter.isEmpty()) {
            if (Memory.getCountOfTracked() == 0) {
                rootView.findViewById(R.id.filter_tip).setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.status)).setText(R.string.no_tracks);
            }else{
                rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);
                ((TextView) rootView.findViewById(R.id.status)).setText(getAdapterEmptyText());
            }
        } else {
            rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);
        }

    }


    @Override
    public void bindContentListener(){
        Memory.addOnlineOnceListener(contentListener);
    }

    @Override
    public void bindGlobalListener() {
        Helper.setTrackUpdatedListener(this.trackUpdateListener);
    }

    @Override
    public void bindNewItemListener() {
        Memory.addOnlineListener(((UpdatesAdapter) adapter).newUpdateListener);
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        if(adapter!=null) {
            Memory.removeOnlineListener(((UpdatesAdapter) adapter).newUpdateListener);
        }
    }

}
