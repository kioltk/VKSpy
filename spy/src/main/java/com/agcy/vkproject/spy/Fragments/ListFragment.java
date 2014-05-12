package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.R;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class ListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected Context context;
    protected final Helper.InitializationListener initializationListener = new Helper.InitializationListener() {
        @Override
        public void onLoadingEnded() {
            startLoading();
        }

        @Override
        public void onDownloadingEnded() {
            startLoading();
        }
    };
    protected BaseAdapter adapter;
    protected Thread task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Helper.isLoaded()){
            startLoading();
        }else{
            Helper.addInitializationListener(initializationListener);
        }
        context = getActivity();
        setRetainInstance(true);
    }

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_list, container, false);
        fillListView();
        return rootView;
    }
    public void startLoading(){
        if(task==null){

            final Handler handler = new Handler();

            task = new Thread(new Runnable() {
                @Override
                public void run() {
                    adapter = adapter();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onLoad();
                        }
                    });
                }
            });
            task.start();
        }
    }
    protected void onLoad(){

        fillListView();
        task = null;
    }
    protected void fillListView(){


        if(adapter!=null && rootView != null) {
            (rootView.findViewById(R.id.loading)).setVisibility(View.GONE);
            if (adapter.isEmpty()) {
                (rootView.findViewById(R.id.status)).setVisibility(View.VISIBLE);
            } else {
                (rootView.findViewById(R.id.status)).setVisibility(View.GONE);
                ((ListView) rootView.findViewById(R.id.list)).setAdapter(adapter);
            }
        }
    }
    public abstract BaseAdapter adapter();
}
