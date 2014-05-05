package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    protected final Context context;
    protected final Helper.OnInitializationEndListener onInitilizationEndListener  = new Helper.OnInitializationEndListener() {
        @Override
        public void onEnd() {
            startLoading();
        }
    };
    protected BaseAdapter adapter;
    protected Thread task;
    protected boolean loading = false;


    public ListFragment(Context context) {
        this.context = context;
        if(Helper.isInitialized()){
            startLoading();
        }else{
            Helper.addOnInitializationEndListener(onInitilizationEndListener);
        }
    }
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_list, container, false);
        if(!loading)
            fillListView();
        return rootView;
    }
    public void startLoading(){
        if(task==null){
            loading = true;
            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    loading = false;
                    fillListView();
                    return true;
                }
            });
            task = new Thread(new Runnable() {
                @Override
                public void run() {
                    adapter = adapter();
                    handler.sendMessage(new Message());
                }
            });
            task.start();
        }
    }

    protected void fillListView(){


        if(adapter!=null && rootView != null) {
            (rootView.findViewById(R.id.loading)).setVisibility(View.GONE);
            if (adapter.isEmpty()) {
                (rootView.findViewById(R.id.status)).setVisibility(View.VISIBLE);
            } else {
                ((ListView) rootView.findViewById(R.id.list)).setAdapter(adapter);
            }
        }
    }
    public abstract BaseAdapter adapter();
}
