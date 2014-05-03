package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.UserActivity;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 27-Apr-14.
 */
public class UpdatesWithOwnerAdapter extends UpdatesAdapter {

    public UpdatesWithOwnerAdapter(ArrayList<? extends Update> updates, Context context) {
        super(updates, context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item= getItem(position);
        if(item instanceof UpdateItem) {
            final UpdateItem updateItem = ((UpdateItem) item);


            View view = updateItem.getViewWithOwner(context);

            view.setOnClickListener(new View.OnClickListener() {
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

            return view;
        }
        return item.getView(context);
    }

}
