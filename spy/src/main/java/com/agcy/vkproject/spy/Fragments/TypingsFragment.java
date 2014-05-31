package com.agcy.vkproject.spy.Fragments;

import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Listeners.NewUpdateListener;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;

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


    @Override
    public int getAdapterEmptyText() {
        return R.string.no_typings;
    }

    @Override
    protected void onContentBinded() {
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
