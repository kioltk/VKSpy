package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.R;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class OnlineAdapter extends BaseAdapter {

    protected final ArrayList<Online> onlines;
    protected final Context context;

    public OnlineAdapter(ArrayList<Online> onlines, Context context){
        this.onlines = onlines;
        this.context = context;
    }

    @Override
    public int getCount() {
        return onlines.size();
    }

    @Override
    public Object getItem(int position) {
        return onlines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_online, null);

        Online item = (Online) getItem(position);

        ((TextView)rootView.findViewById(R.id.since)).setText(""+item.getSinceShort());
        ((TextView)rootView.findViewById(R.id.till)).setText(""+item.getTillShort());

        return rootView;
    }
}
