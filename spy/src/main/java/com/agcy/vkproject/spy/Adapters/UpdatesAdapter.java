package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UpdatesAdapter extends BaseAdapter {

    protected final Context context;
    protected final ItemHelper.ObservableUpdatesArray items;

    public UpdatesAdapter(ArrayList<? extends Update> updates, Context context){
        this.items = new ItemHelper.ObservableUpdatesArray(updates);
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

        Item item = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup rootView = null;
        ViewGroup timelineContainer = null;
        ViewGroup contentContainer = null;
        View contentView = null;
        View timelineView = null;
        if (item instanceof UpdateItem) {
            // это апдейт? толгда сингл или даубл
            rootView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_template, null);

            if (item instanceof OnlineItem && ((OnlineItem) item).getContent().isStreak()) {
                timelineView = inflater.inflate(R.layout.list_item_timeline_double, null);
            } else
                timelineView = inflater.inflate(R.layout.list_item_timeline_single, null);


            contentView = item.getView(context);

            if (items.isLast(position)) {
                ViewGroup bottomTimelineView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_end, null);

                LinearLayout rootViewTemp = new LinearLayout(context);
                rootViewTemp.setOrientation(LinearLayout.VERTICAL);

                rootViewTemp.addView(rootView);
                rootViewTemp.addView(bottomTimelineView);
                rootView = rootViewTemp;
            }
        } else {
            // это не апдейт?
            if (item instanceof DateItem) {
                rootView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_center_template, null);
                contentView = ((DateItem)item).getCenterView(context);

                ViewGroup topTimelineContainer = (ViewGroup) rootView.findViewById(R.id.topTimelineContainer);
                ViewGroup bottomTimelineContainer = (ViewGroup) rootView.findViewById(R.id.bottomTimelineContainer);
                contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
                contentContainer.addView(contentView);

                ViewGroup bottomTimelineView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_start,null);
                bottomTimelineContainer.addView(bottomTimelineView);

                if(position==0) {
                    topTimelineContainer.setVisibility(View.GONE);
                }
                else{
                    ViewGroup topTimelineView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_end,null);
                    topTimelineContainer.addView(topTimelineView);
                }

                //topTimelineContainer.addView(timelineView);
                return rootView;
            }

            //timelineView =(RelativeLayout) inflater.inflate(R.layout.list_item_timeline_middle_break, null);
        }




        timelineContainer = (ViewGroup) rootView.findViewById(R.id.timelineContainer);
        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        timelineContainer.addView(timelineView);
        contentContainer.addView(contentView);
        return rootView;

    }
}
