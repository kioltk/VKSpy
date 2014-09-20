package com.happysanta.vkspy.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.happysanta.vkspy.Adapters.UpdatesAdapter;
import com.happysanta.vkspy.Adapters.UpdatesWithOwnerAdapter;
import com.happysanta.vkspy.Core.Memory;
import com.happysanta.vkspy.Helper.Time;
import com.happysanta.vkspy.Listeners.NewUpdateListener;
import com.happysanta.vkspy.Models.Typing;
import com.happysanta.vkspy.Models.Update;
import com.happysanta.vkspy.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class TypingsFragment extends UpdatesFragment {

    private NewUpdateListener contentListener = new NewUpdateListener() {
        @Override
        public void newItem(Update item) {
            createContent();
        }
    };
    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("notifications_chat_typing_enabled")) {
                recreateContent();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(changeListener);
    }

    @Override
    public BaseAdapter adapter() {

        boolean chatsEnabled = true;
        ArrayList<Typing> items;

        FragmentActivity activity = getActivity();
        if(activity!=null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

            chatsEnabled = prefs.getBoolean("notifications_chat_typing_enabled", true);
        }
        if (chatsEnabled)
            items = Memory.getTypings();
        else
            items = Memory.getTypings("chatid = 0");
        return new UpdatesWithOwnerAdapter(items, context);
    }
    Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(task);
        }
    };
    Runnable task = new Runnable() {
        @Override
        public void run() {

            ListView listView = getListView();
            if(listView==null)
                return;
            for (int i = 0; i < listView.getChildCount(); i++) {
                View view = listView.getChildAt(i);
                View timeView = view.findViewById(R.id.time);
                if (timeView == null)
                    continue;
                Object tag = timeView.getTag();
                if (tag == null)
                    continue;
                if (tag instanceof Typing) {
                    ((TextView) timeView).setText(((Typing) tag).getSmartTime());
                    continue;
                }
                if (tag instanceof Integer) {
                    ((TextView) timeView).setText(Time.getSmartDate((Integer) tag));

                }


            }
        }
    };
    @Override
    public int getAdapterEmptyText() {
        return R.string.no_typings;
    }

    @Override
    protected void onContentBinded() {
        bindTimer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //View button = inflater.inflate(R.layout.filter_typings_button, (ViewGroup) rootView, true);
        return rootView;
    }
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(task);
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        bindTimer();
    }

    private void bindTimer() {
        try {
            timer.schedule(timerTask, 0, 60 * 1000L);
        }catch (Exception ignored){
        }
    }


    @Override
    public void bindContentListener() {

            Memory.addTypingOnceListener(contentListener);

    }

    @Override
    public void bindGlobalListener() {

    }

    @Override
    public void bindNewItemListener() {
        Memory.addTypingListener(((UpdatesAdapter)adapter).newUpdateListener);
    }
}
