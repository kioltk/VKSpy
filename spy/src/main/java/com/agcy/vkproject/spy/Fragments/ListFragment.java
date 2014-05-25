package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;
import com.bugsense.trace.BugSenseHandler;

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
            createContent();
        }

        @Override
        public void onDownloadingEnded() {
            createContent();
        }
    };
    protected BaseAdapter adapter;
    protected Thread task;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Helper.isLoaded()){
            createContent();
        }else{
            Helper.addInitializationListener(initializationListener);
        }
        context = getActivity();
    }

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflateRootView(inflater, container);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                createContent();
            }
        });
        return rootView;
    }

    protected View inflateRootView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        if(hasHeader())
            (view.findViewById(R.id.faux_padding)).setVisibility(View.VISIBLE);
        else
            (view.findViewById(R.id.faux_padding)).setVisibility(View.GONE);

        return view;
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
                    if(listView.getHeaderViewsCount()==0)
                        listView.addHeaderView(getListViewHeaderView(),null,false);
                }catch(Exception exp){
                }
                listView.setAdapter(adapter);
                AbsListView.OnScrollListener scrollListener = getScrollListener();
                if(scrollListener!=null)
                    listView.setOnScrollListener(scrollListener);
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

}
