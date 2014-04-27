package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by kiolt_000 on 27-Apr-14.
 */
public class OwnerAdapterHelper {
    public static VKApiUser getOwner(int userid){
        return Memory.getUserById(userid);
    }
    public static View getOwnerView(int userid,Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_user, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        ImageView status = (ImageView) rootView.findViewById(R.id.status);
        VKApiUser user = getOwner(userid);
        name.setText(user.first_name+" "+user.last_name);
        if(user.online) {
            status.setVisibility(View.VISIBLE);
            //if (user.online_mobile) status.setText("В сети с мобильного");
        }
        ImageLoader.getInstance().displayImage(user.photo_100,photo);

        return rootView;
    }
}
