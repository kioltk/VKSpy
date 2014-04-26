package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UserListAdapter extends BaseAdapter {

    private final VKUsersArray friends;
    private final Context context;

    public UserListAdapter(VKUsersArray friends,Context context) {
        this.friends = friends;
        this.context = context;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return friends.get(0).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_user, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView status = (TextView) rootView.findViewById(R.id.status);
        VKApiUser user = (VKApiUser) getItem(position);
        name.setText(user.first_name+" "+user.last_name);
        if(!user.online)
            status.setVisibility(View.GONE);
        else if(user.online_mobile) status.setText("В сети с мобильного");
        ImageLoader.getInstance().displayImage(user.photo_100,photo);
        return rootView;
    }
}
