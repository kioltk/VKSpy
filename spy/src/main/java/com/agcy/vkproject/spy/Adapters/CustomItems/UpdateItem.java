package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class UpdateItem extends Item {

    protected final Update update;

    public UpdateItem(Update update){
        this.update = update;
    }
    @Override
    public abstract View getView(Context context);
    public View getViewWithOwner(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ownerView = inflater.inflate(R.layout.list_item_user, null);

        ImageView photo = (ImageView) ownerView.findViewById(R.id.photo);
        TextView name = (TextView) ownerView.findViewById(R.id.name);
        ImageView status = (ImageView) ownerView.findViewById(R.id.status_image);
        VKApiUser user = update.getOwner();
        name.setText(user.first_name+" "+user.last_name);
        if(user.online) {
            status.setVisibility(View.VISIBLE);
            //if (user.online_mobile) status.setText("В сети с мобильного");
        }
        ImageLoader.getInstance().displayImage(user.photo_100,photo);
        View contentView = getView(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(ownerView);
        linearLayout.addView(contentView);

        return linearLayout;
    }
    @Override
    public Update getContent() {
        return update;
    }
}
