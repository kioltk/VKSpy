package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 05-May-14.
 */
public class UserItem extends Item {
    private final VKApiUserFull user;

    public UserItem(VKApiUserFull user){
        this.user = user;
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_user, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        ImageView status = (ImageView) rootView.findViewById(R.id.status_image);

        name.setText(user.first_name+" "+user.last_name);
        if(user.online) {
            status.setVisibility(View.VISIBLE);
            if (user.online_mobile)
                status.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_online_mobile));
        }
        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(),photo);
        return rootView;
    }

    @Override
    public VKApiUserFull getContent() {
        return user;
    }
}
