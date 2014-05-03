package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Models.Update;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UpdatesAdapter extends BaseAdapter {

    protected final Context context;
    protected final ItemHelper.ObservableArray items;

    public UpdatesAdapter(ArrayList<? extends Update> updates, Context context){
        this.items = new ItemHelper.ObservableArray(updates);
        this.context = context;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {

        if(items.size()-15<position){
            if(items.convertMore(15))
                notifyDataSetChanged();
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        return getItem(position).getView(context);
    }
}
