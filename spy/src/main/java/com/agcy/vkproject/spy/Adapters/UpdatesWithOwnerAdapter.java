package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class UpdatesWithOwnerAdapter extends UpdatesAdapter {
    public UpdatesWithOwnerAdapter(ArrayList<? extends Update> items, Context context) {
        super(items, context);
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final int height = (int) Helper.convertToDp(54);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Item item= getItem(position);
        ViewGroup rootView =(RelativeLayout) inflater.inflate(R.layout.list_item_timeline_left_template, null);
        ViewGroup timelineContainer = null;
        ViewGroup contentContainer = null;
        View contentView = null;
        View timelineView = inflater.inflate(R.layout.list_item_timeline_single, null);

        //todo: more items!!1
        if(item instanceof UpdateItem) {
            final UpdateItem updateItem = ((UpdateItem) item);


            contentView = updateItem.getViewWithOwner(context);

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent showUserIntent = new Intent(context, UserActivity.class);
                    showUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", updateItem.getContent().getOwner().id);
                    showUserIntent.putExtras(bundle);
                    context.startActivity(showUserIntent);
                }
            });


        }else{
            contentView = item.getView(context);
            timelineView =  inflater.inflate(R.layout.list_item_timeline_single_big, null);
        }
        if (item.isNew()) {

            item.setNew(false);
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(700);
            final ViewGroup finalRootView = rootView;
            finalRootView.setLayoutParams(
                    new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    )
            );

            Log.i("AGCY SPY ANIMATION", "Default height :" + height);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int convertedHeight = (int) (height * ((Float) valueAnimator.getAnimatedValue()))+1;
                    Log.i("AGCY SPY ANIMATION", "Height :" + convertedHeight);

                    finalRootView.setLayoutParams(
                            new AbsListView.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    convertedHeight
                            )
                    );

                }

            });
            anim.start();
        }else{
            rootView.setLayoutParams(
                    new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            height
                    )
            );
        }
        if(position==0){
            if(item instanceof DateItem) {
                if((Integer)item.getContent()!= 0) {
                    lastTodayItem = item;
                    lastTodayView = rootView;
                }
            }
            timelineView = inflater.inflate(R.layout.list_item_timeline_start, null);
        }else {
            if (position == items.size()-1) {

                timelineView = inflater.inflate(R.layout.list_item_timeline_end_long, null);
                rootView.setLayoutParams(
                        new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int)(height * 1.75)
                        )
                );
                if (!items.isLast(position)) {

                    View circle = timelineView.findViewById(R.id.circle);

                    ScaleAnimation blinkAnimation = new ScaleAnimation(0.5f, 1, 0.5f, 1, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, 0.5f);
                    blinkAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    blinkAnimation.setDuration(750);
                    blinkAnimation.setRepeatMode(Animation.REVERSE);
                    blinkAnimation.setRepeatCount(Animation.INFINITE);
                    circle.startAnimation(blinkAnimation);
                    final Handler handler = new Handler();
                    final View finalTimelineView = timelineView;
                    Log.i("AGCY SPY","Load more");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(loading)
                                return;
                            loading = true;

                            try {
                                Thread.sleep(2500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            items.convertMore(15);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loading = false;
                                    notifyDataSetChanged();
                                    //finalTimelineView.setVisibility(View.GONE);
                                }
                            });
                        }
                    })       .start()
                    ;
                }
            }
        }
        timelineContainer = (ViewGroup) rootView.findViewById(R.id.timelineContainer);
        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        timelineContainer.addView(timelineView);
        contentContainer.addView(contentView);

        return rootView;
    }
}
