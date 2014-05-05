package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class UpdatesWithOwnerAdapter extends UpdatesAdapter {
    public UpdatesWithOwnerAdapter(ArrayList<? extends Update> items, Context context) {
        super(items, context);
    }

    @Override
    public Item getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup rootView =(RelativeLayout) inflater.inflate(R.layout.list_item_timeline_left_template, null);
        ViewGroup timelineContainer = null;
        ViewGroup contentContainer = null;
        View contentView = null;
        View timelineView = inflater.inflate(R.layout.list_item_timeline_single, null);

        Item item= getItem(position);
        if(item instanceof UpdateItem) {
            final UpdateItem updateItem = ((UpdateItem) item);


            contentView = updateItem.getViewWithOwner(context);

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent showUserIntent = new Intent(context, UserActivity.class);
                    showUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", updateItem.getContent().getOwner().id);
                    showUserIntent.putExtras(bundle);
                    context.startActivity(showUserIntent);
                }
            });


        }else{
            contentView = item.getView(context);
            timelineView =  inflater.inflate(R.layout.list_item_timeline_single_big, null);
        }
        if(position==0){
            timelineView = inflater.inflate(R.layout.list_item_timeline_start, null);
        }else {
            if (items.isLast(position)) {
                timelineView = inflater.inflate(R.layout.list_item_timeline_end, null);
            }
        }
        timelineContainer = (ViewGroup) rootView.findViewById(R.id.timelineContainer);
        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        timelineContainer.addView(timelineView);
        contentContainer.addView(contentView);

        return rootView;
    }
}
