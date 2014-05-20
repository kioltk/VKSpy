package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;

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
                    if(adapter==null || adapter.isEmpty()) {
                        adapter = adapter();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onLoad();
                            }
                        });
                    }
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
                final ListView listView = (ListView) rootView.findViewById(R.id.list);
                listView.setAdapter(adapter);
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    private int visibleItemCount;
                    private int firstVisibleItem;
                    public boolean firstLoading = true;

                    void loadImages(){
                        for(int i = firstVisibleItem;i < visibleItemCount+ firstVisibleItem;i++) {
                            Object item = adapter.getItem(i);
                            if(item instanceof UpdateItem){
                                UpdateItem updateItem = (UpdateItem) item;
                                updateItem.loadImage(listView.getChildAt(i-firstVisibleItem));
                            }
                        }
                    }

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            loadImages();
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                        this.firstVisibleItem = firstVisibleItem;
                        this.visibleItemCount = visibleItemCount;

                        if(firstLoading && visibleItemCount!=0){
                            firstLoading = false;
                            loadImages();
                        }

                    }
                });
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        UpdateItem updateItem = (UpdateItem) parent.getItemAtPosition(position);

                        Intent showUserIntent = new Intent(context, UserActivity.class);
                        showUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle bundle = new Bundle();
                        bundle.putInt("id", updateItem.getContent().getOwner().id);
                        showUserIntent.putExtras(bundle);
                        context.startActivity(showUserIntent);
                    }
                });
            }
        }
    }



    public abstract BaseAdapter adapter();
}
