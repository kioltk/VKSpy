package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Memory;

/**
 * A placeholder fragment containing a simple view.
 */
public class TypingsFragment extends ListFragment {


    @Override
    public BaseAdapter adapter() {
        return new UpdatesWithOwnerAdapter(Memory.getTyping(),context);
    }


}
