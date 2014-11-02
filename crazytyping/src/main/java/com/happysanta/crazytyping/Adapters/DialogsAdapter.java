package com.happysanta.crazytyping.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.happysanta.crazytyping.Adapters.CustomItems.DialogItem;
import com.happysanta.crazytyping.Core.Helper;
import com.happysanta.crazytyping.Models.Dialog;
import com.happysanta.crazytyping.R;
import com.happysanta.crazytyping.Views.TimelineView;

import java.util.ArrayList;

/**
* Created by kioltk on 6/19/14.
*/
public class DialogsAdapter extends BaseAdapter {
    private final ArrayList<DialogItem> items;
    private final Context context;

    public DialogsAdapter(ArrayList<Dialog> items, Context context) {
        this.items = new ArrayList<DialogItem>();
        for (Dialog item : items) {
            this.items.add(new DialogItem(item));
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public DialogItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getContent().getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rootView;
        /*final int height = (int) Helper.convertToDp(54);

        ViewGroup contentContainer = null;
        View contentView = null;
        */

        DialogItem item = getItem(position);
        if (convertView != null) {
            rootView = convertView;

            item.reconvert(context, rootView);
        } else {

            rootView = item.getView(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }




        //long elapsedTime = System.nanoTime() - startTime;
        //Log.i("AGCY SPY TIMER","Item created in "
        //      + elapsedTime / 1000000);
        rootView.setTag(item.getContent().getId());
        return rootView;
    }
}
