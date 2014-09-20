package com.happysanta.vkspy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.happysanta.vkspy.Adapters.CustomItems.PreferenceItem;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class PreferenceAdapter extends BaseAdapter {


    private final ArrayList<PreferenceItem> preferences;
    private final Context context;

    public PreferenceAdapter(Context context, ArrayList<PreferenceItem> preferences){
        this.preferences = preferences;
        this.context = context;
    }

    @Override
    public int getCount() {
        return preferences.size();
    }

    @Override
    public PreferenceItem getItem(int position) {
        return preferences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PreferenceItem item = getItem(position);
        View view = item.getView(context);
        return view;
    }

}
