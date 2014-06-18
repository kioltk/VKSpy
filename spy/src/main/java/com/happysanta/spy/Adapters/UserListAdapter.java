package com.happysanta.spy.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.happysanta.spy.Adapters.CustomItems.HeaderItem;
import com.happysanta.spy.Adapters.CustomItems.Item;
import com.happysanta.spy.Adapters.CustomItems.UserItem;
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
        items = new ItemHelper.ObservableUsersArray(users, true);
    }
    public UserListAdapter(ItemHelper.ObservableUsersArray items, Context context){
        this.items = items;
        this.context = context;
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
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        Item item = getItem(position);
        if(item instanceof UserItem)
            return ((UserItem)item).getId();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        View rootView;
        if(convertView!=null && convertView.getTag()!=null && item instanceof UserItem)
            rootView = ((UserItem)item).reconvert(context, convertView);
        else
            rootView= item.getView(context);
        if(item instanceof UserItem)
            rootView.setTag("user");
        if(!items.isLast(position)){
            if(getItem(position+1) instanceof HeaderItem){
                UserItem userItem = (UserItem) item;
                userItem.removeDivider(rootView);
            }else{
                if(item instanceof UserItem) {

                    ((UserItem)item).setDivider(rootView);
                }
            }
        }
        return rootView;
    }


    public ArrayList<Item> getItems() {
        return items.convertedItems;
    }
}
