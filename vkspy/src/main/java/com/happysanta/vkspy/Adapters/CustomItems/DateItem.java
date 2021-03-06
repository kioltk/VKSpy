package com.happysanta.vkspy.Adapters.CustomItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Helper.Time;
import com.happysanta.vkspy.R;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class DateItem extends Item {

    private int time;

    View rootView;
    public DateItem(int unix){
        this.time = unix;
    }
    @Override
    public View getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.list_item_date, null);
        TextView timeView = ((TextView) rootView.findViewById(R.id.time));
        timeView.setText(getDate());

        timeView.setTag(time);
        return rootView;
    }
    public String getDate(){
        return Time.getSmartDate(time);
    }
    @Override
    public Integer getContent() {
        return time;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public View getCenterView(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.list_item_date_center, null);

        ((TextView)rootView.findViewById(R.id.time)).setText( getDate());
        return rootView;
    }

    public void recreate(Integer unix) {
        this.time = unix;
        if(rootView!=null){

            final TextView timeView = ((TextView) rootView.findViewById(R.id.time));
            AlphaAnimation recreateAnimation = new AlphaAnimation(1, 0.5f);
            recreateAnimation.setInterpolator(new DecelerateInterpolator());
            recreateAnimation.setDuration(600);
            recreateAnimation.setRepeatMode(Animation.REVERSE);
            recreateAnimation.setRepeatCount(1);
            recreateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    timeView.setText(getDate());
                }
            });
            timeView.startAnimation(recreateAnimation);
        }
    }


    public boolean isToday() {
        return Time.isToday(this.time);
    }

    public boolean isNow() {
        return getContent() == Helper.NOW;
    }

    @Override
    public void setDeleted() {
        super.setDeleted();
        if (rootView != null) {

            final TextView timeView = ((TextView) rootView.findViewById(R.id.time));
            AlphaAnimation recreateAnimation = new AlphaAnimation(1, 0);
            recreateAnimation.setInterpolator(new DecelerateInterpolator());
            recreateAnimation.setDuration(600);
        }
    }
}
