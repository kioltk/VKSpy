package com.agcy.vkproject.spy.Fragments;

import android.os.Handler;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Listeners.NewUpdateListener;
import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class TypingsFragment extends UpdatesFragment {

    private NewUpdateListener contentListener = new NewUpdateListener() {
        @Override
        public void newItem(Update item) {
            createContent();
        }
    };
    @Override
    public BaseAdapter adapter() {
        return new UpdatesWithOwnerAdapter(Memory.getTyping(),context);
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
                    ((TextView) timeView).setText(Helper.getSmartDate((Integer) tag));

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
