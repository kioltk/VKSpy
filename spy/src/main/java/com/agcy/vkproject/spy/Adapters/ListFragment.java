package com.agcy.vkproject.spy.Adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.agcy.vkproject.spy.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private final UpdatesAdapter adapter;


    public ListFragment(UpdatesAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ListView rootView = (ListView) inflater.inflate(R.layout.fragment_list, container, false);
        rootView.setAdapter(adapter);
        return rootView;
    }
}
