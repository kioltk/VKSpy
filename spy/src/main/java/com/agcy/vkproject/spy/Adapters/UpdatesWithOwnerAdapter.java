package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;

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
        if(item instanceof UpdateItem)
            return ((UpdateItem) item).getViewWithOwner(context);
        return item.getView(context);
    }

}
