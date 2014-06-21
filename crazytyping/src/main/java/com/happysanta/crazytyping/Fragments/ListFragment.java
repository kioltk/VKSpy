package com.happysanta.crazytyping.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import com.actionbarpulltorefresh.library.PullToRefreshLayout;
import com.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import com.happysanta.crazytyping.Adapters.CustomItems.UpdateItem;
import com.happysanta.crazytyping.MainActivity;
import com.happysanta.crazytyping.R;
import com.bugsense.trace.BugSenseHandler;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class ListFragment extends Fragment implements AdapterView.OnItemClickListener
, OnRefreshListener{

    protected Context context;
    protected BaseAdapter adapter;
    protected Thread task;
    private PullToRefreshLayout mPullToRefreshLayout;

    protected void onDownloadingEnded(){
        createContent();
    }
    protected void onLoadingEnded(){
        createContent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        fragment = this;
    }

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Set title in Fragment for display purposes.


        return rootView;
    }

    @Override
    public void onRefreshStarted(View view) {

        MainActivity.updateDialogs(
                new Runnable() {
                    @Override
                    public void run() {
                        ListFragment.setRefreshComplete();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        ListFragment.setRefreshFailed();

                    }
                }
        );
    }
    public static ListFragment fragment;
    public void setRefresh(){
        if(mPullToRefreshLayout!=null)
            mPullToRefreshLayout.setRefreshing(true);
    }
    public static void setRefreshFailed(){
        if(fragment!=null){
            if(fragment.mPullToRefreshLayout!=null)
                fragment.mPullToRefreshLayout.setRefreshFailed();
        }
    }
    public static void setRefreshComplete(){
        if(fragment!=null){
            if(fragment.mPullToRefreshLayout!=null)
                fragment.mPullToRefreshLayout.setRefreshComplete();
        }
    }
    public static void showRefresh(){
        if(fragment!=null){
            fragment.setRefresh();
        }
    }
    public void createContent(){
        if(task==null){

            final Handler handler = new Handler();

            task = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(adapter==null || adapter.isEmpty()) {
                        boolean adapterCreated = false;
                        while(!adapterCreated) {
                            try {
                                adapter = adapter();
                                adapterCreated = true;
                            }catch (Exception exp){

                            }
                        }
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bindContent();
                        }
                    });
                    task = null;
                }
            });
            task.start();
        }
    }
    public void recreateContent(){
        if(task!=null) {
            task.interrupt();
        }
        if(rootView!=null){
            rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.list).setVisibility(View.GONE);
            rootView.findViewById(R.id.status).setVisibility(View.GONE);

        }
        final Handler handler = new Handler();

        task = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean adapterCreated = false;
                while(!adapterCreated) {
                    try {
                        adapter = adapter();
                        adapterCreated = true;
                    }catch (Exception exp){
                        BugSenseHandler.sendException(exp);
                    }
                }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bindContent();
                        }
                    });
                    task = null;
            }
        });
        task.start();

    }

    protected abstract BaseAdapter adapter();

    protected abstract void onContentBinded();
    protected void bindContent(){


        if(adapter!=null && rootView != null) {
            (rootView.findViewById(R.id.loading)).setVisibility(View.GONE);
            if (adapter.isEmpty()) {

                TextView statusView = (TextView) rootView.findViewById(R.id.status);
                statusView.setVisibility(View.VISIBLE);
                statusView.setText(getAdapterEmptyText());

            } else {

                rootView.findViewById(R.id.status).setVisibility(View.GONE);
                final ListView listView = getListView();
                listView.setVisibility(View.VISIBLE);
                listView.setAdapter(null);
                if(hasHeader())
                try {
                    View headerView;
                    if(listView.getHeaderViewsCount()==0 && (headerView=getListViewHeaderView())!=null)
                        listView.addHeaderView(headerView,null,false);
                }catch(Exception exp){
                }
                listView.setAdapter(adapter);
                AbsListView.OnScrollListener scrollListener = getScrollListener();
                if(scrollListener!=null)
                    listView.setOnScrollListener(scrollListener);
                listView.setOnItemClickListener(this);
            }
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                onContentBinded();
            }
        });
    }

    public int getAdapterEmptyText() {
        return R.string.no_events;
    }
    public ListView getListView(){
        if(rootView!=null){
            return (ListView) rootView.findViewById(R.id.list);
        }
        return null;
    }

    public View getListViewHeaderView(){
        TextView text = new TextView(context);
        text.setText("IM HEADER!!1");
        text.setGravity(Gravity.CENTER);
        return text;
    }

    public AbsListView.OnScrollListener getScrollListener() {
        return null;
    }

    protected boolean hasHeader(){
        return false;
    }

    @Override
    public abstract void onItemClick(AdapterView<?> parent, View view, int position, long id);
}
