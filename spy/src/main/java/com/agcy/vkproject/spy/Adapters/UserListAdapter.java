package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UserListAdapter extends BaseAdapter {

    private final Context context;
    private final ItemHelper.ObservableUsersArray items;
    public UserListAdapter(ArrayList<VKApiUserFull> users,Context context) {
        this.context = context;
        items = new ItemHelper.ObservableUsersArray(users);
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
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
        View rootView = item.getView(context);
        return rootView;
    }
}
