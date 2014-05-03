package com.agcy.vkproject.spy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class OnlineItem extends UpdateItem {

    public OnlineItem(Online online) {
        super(online);
    }

    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_item_online, null);

        View divider = rootView.findViewById(R.id.divider);



        final TextView since = ((TextView) rootView.findViewById(R.id.since));
                since.setText("" + getContent().getSinceShort());
        final TextView till = ((TextView) rootView.findViewById(R.id.till));
                till.setText("" + getContent().getTillShort());

        final AlphaAnimation sinceAlphaAnimation = new AlphaAnimation(0,1);
        sinceAlphaAnimation.setInterpolator(new AccelerateInterpolator());
        sinceAlphaAnimation.setDuration(700);


        final AlphaAnimation tillAlphaAnimation = new AlphaAnimation(0,1);
        tillAlphaAnimation.setInterpolator(new AccelerateInterpolator());
        tillAlphaAnimation.setDuration(700);
        tillAlphaAnimation.setStartOffset(700);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,1,1);
        scaleAnimation.setDuration(1000);


        divider.startAnimation(scaleAnimation);

        since.startAnimation(sinceAlphaAnimation);
        till.startAnimation(tillAlphaAnimation);
        return rootView;
    }

    @Override
    public Online getContent() {
        return (Online) update;
    }
}
