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

        final int height = (int) Helper.convertToDp(54);

        View rootView;
        ViewGroup contentContainer = null;
        View contentView = null;
        boolean reconvert = false;

        if (convertView != null) {
            rootView = convertView;
                reconvert = true;

        } else {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.list_item_timeline_left_template, null);
        }


        DialogItem item = getItem(position);


        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        TimelineView timelineView = (TimelineView) rootView.findViewById(R.id.timelineView);
        timelineView.setTimelineNormal();

        if (reconvert) {
            item.reconvert(context, rootView);
        } else {
            contentView = item.getView(context);
        }

        View background = rootView.findViewById(R.id.background);
        RelativeLayout.LayoutParams containerParams =
                (RelativeLayout.LayoutParams) contentContainer.getLayoutParams();
        RelativeLayout.LayoutParams backgroundParams = (RelativeLayout.LayoutParams) background.getLayoutParams();
        if (position == 0) {
            rootView.setLayoutParams(
                    new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (int) (height * 2)
                    )
            );
            containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            containerParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            containerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            contentContainer.setLayoutParams(containerParams);

            backgroundParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            backgroundParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            background.setLayoutParams(backgroundParams);

            timelineView.setTimelineType(TimelineView.TIMELINE_START_LONG);

        } else {
            if (position == items.size() - 1) {
                rootView.setLayoutParams(
                        new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) (height * 2)
                        )
                );
                containerParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                containerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                contentContainer.setLayoutParams(containerParams);

                backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                backgroundParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                background.setLayoutParams(backgroundParams);


                timelineView.setTimelineType(TimelineView.TIMELINE_END_LONG);
            } else {
                rootView.setLayoutParams(
                        new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                height
                        )
                );
                containerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                contentContainer.setLayoutParams(containerParams);

                backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                background.setLayoutParams(backgroundParams);

                timelineView.setTimelineNormal();
            }
        }

        if (contentView != null) {
            contentContainer.removeAllViews();
            contentContainer.addView(contentView);
        }
        //long elapsedTime = System.nanoTime() - startTime;
        //Log.i("AGCY SPY TIMER","Item created in "
        //      + elapsedTime / 1000000);
        rootView.setTag(item.getContent().getId());
        return rootView;
    }
}
