package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.CustomItems.Interfaces.LoadableImage;
import com.agcy.vkproject.spy.Adapters.CustomItems.Interfaces.Reconvertable;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 05-May-14.
 */
public class UserItem extends Item implements LoadableImage,Reconvertable {
    private final VKApiUserFull user;
    private boolean imageLoaded;

    public UserItem(VKApiUserFull user){
        this.user = user;
    }
    @Override
    public View getView(Context context) {
        View rootView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.list_item_user, null);

        reconvert(context, rootView);
        return rootView;
    }

    @Override
    public VKApiUserFull getContent() {
        return user;
    }

    public void removeDivider(View rootView) {
        View divider = rootView.findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
    }

    @Override
    public void loadImage(View rootView) {
        if(rootView!=null) {
            imageLoaded = true;
            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);

            ImageLoader.getInstance().displayImage(user.getBiggestPhoto(), photo);
        }
    }
    @Override
    public void placeHolder(View rootView) {
        if(rootView!=null) {
            imageLoaded = true;
            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            photo.setImageResource(R.drawable.user_placeholder);
        }
    }

    @Override
    public View reconvert(Context context, View rootView) {
        TextView name = (TextView) rootView.findViewById(R.id.name);
        ImageView status = (ImageView) rootView.findViewById(R.id.status_image);

        name.setText(user.first_name+" "+user.last_name);
        if(user.online) {
            status.setVisibility(View.VISIBLE);
            if (user.online_mobile)
                status.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_online_mobile));
        }else{
            status.setVisibility(View.GONE);
        }
        if(imageLoaded)
            loadImage(rootView);
        else
            placeHolder(rootView);
        return rootView;
    }

    public void setDivider(View rootView) {
        View divider = rootView.findViewById(R.id.divider);
        divider.setVisibility(View.VISIBLE);
    }
}
