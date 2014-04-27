package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.R;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 27-Apr-14.
 */
public class TypingAdapter extends BaseAdapter {
    protected final Context context;
    private final ArrayList<Typing> typings;

    public TypingAdapter(ArrayList<Typing> typings, Context context) {
        this.typings = typings;
        this.context = context;
    }

    @Override
    public int getCount() {
        return typings.size();
    }

    @Override
    public Object getItem(int position) {
        return typings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_typing, null);

        Typing item = (Typing) getItem(position);
        //todo: time converters
        ((TextView)rootView.findViewById(R.id.time)).setText(""+item.time);

        return rootView;
    }
}
