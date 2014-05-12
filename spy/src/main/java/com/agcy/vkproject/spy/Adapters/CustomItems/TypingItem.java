package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class TypingItem extends UpdateItem {

    public TypingItem(Typing typing){
        super(typing);
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.list_item_typing, null);

        ((TextView)rootView.findViewById(R.id.time)).setText(""+ getContent().getSmartTime());
        return rootView;
    }

    @Override
    public View getViewWithOwner(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_update_with_owner, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView time = (TextView) rootView.findViewById(R.id.time);
        VKApiUserFull user = update.getOwner();
        name.setText(user.first_name+" "+user.last_name);

        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(),photo);

        time.setText("" + getContent().getSmartTime());


        return rootView;


    }

    @Override
    public Typing getContent() {
        return (Typing) update;
    }
}
