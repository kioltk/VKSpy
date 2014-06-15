package com.happysanta.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.happysanta.spy.R;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 20-May-14.
 */
public class FilterUserItem extends UserItem {

    private boolean trackedTemp;


    public FilterUserItem(VKApiUserFull user) {
        super(user);
        trackedTemp = user.isTracked();
    }

    @Override
    public View getView(Context context) {

        View view = super.getView(context);

        setTracked(trackedTemp,view);

        return view;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View reconvert(Context context, View rootView) {
        View view = super.reconvert(context, rootView);
        setTracked(trackedTemp,view);
        return view;
    }

    @Override
    public VKApiUserFull getContent() {
        return super.getContent();
    }

    public void setTracked(boolean tracked,View view) {
        this.trackedTemp = tracked;
        if (view == null) {
            return;
        }
        ImageView statusView = (ImageView) view.findViewById(R.id.status_image);
        TextView name = (TextView) view.findViewById(R.id.name);

        statusView.setImageResource(R.drawable.ic_checked);

        if(trackedTemp) {
            name.setSelected(true);
            statusView.setVisibility(View.VISIBLE);
        }else{
            name.setSelected(false);
            statusView.setVisibility(View.GONE);
        }
    }
}
