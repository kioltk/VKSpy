package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;

/**
 * A placeholder fragment containing a simple view.
 */
public class OnlinesFragment extends ListFragment {
    public OnlinesFragment(Context context) {
        super(context);
    }

    @Override
    public BaseAdapter adapter() {
        return new UpdatesWithOwnerAdapter(Helper.convertToStatus(Memory.getOnlines()), context);
    }
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
}
