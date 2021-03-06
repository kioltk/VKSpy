package com.happysanta.vkspy.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.happysanta.vkspy.Adapters.UpdatesAdapter;
import com.happysanta.vkspy.Adapters.UpdatesWithOwnerAdapter;
import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Core.Memory;
import com.happysanta.vkspy.Core.Notificator;
import com.happysanta.vkspy.Listeners.NewUpdateListener;
import com.happysanta.vkspy.Models.Update;
import com.happysanta.vkspy.R;


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
    public void onPause() {
        super.onPause();
        Notificator.onlinesOpened = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Notificator.onlinesOpened = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.filter_users_button, (ViewGroup) rootView, true);

        return rootView;
    }



    @Override
    public BaseAdapter adapter() {

        UpdatesWithOwnerAdapter adapter = new UpdatesWithOwnerAdapter(Helper.convertToStatus(Memory.getTrackedOnlines()), context);
        return adapter;

    }

    @Override
    public void recreateContent() {
        if(rootView!=null)
            rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);

        super.recreateContent();
    }

    @Override
    protected void onContentBinded() {

        if(rootView==null)
            return;
        if (Memory.users.isEmpty() && (!Helper.isDownloaded() || !Helper.isLoaded())){

            rootView.findViewById(R.id.status).setVisibility(View.GONE);
            rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);
            rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            return;
        }
        if (adapter == null || adapter.isEmpty()) {
            if(Helper.isFetching()){

                rootView.findViewById(R.id.status).setVisibility(View.GONE);
                rootView.findViewById(R.id.filter_tip).setVisibility(View.GONE);
                rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                return;
            }
            if (Memory.getCountOfTracked() == 0 ) {
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
