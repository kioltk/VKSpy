package com.happysanta.spy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;

import com.happysanta.spy.Adapters.CustomItems.DateItem;
import com.happysanta.spy.Adapters.CustomItems.Item;
import com.happysanta.spy.Adapters.CustomItems.UpdateItem;
import com.happysanta.spy.Views.TimelineView;
import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.Models.Update;
import com.happysanta.spy.R;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;


public class UpdatesWithOwnerAdapter extends UpdatesAdapter {


    public UpdatesWithOwnerAdapter(ArrayList<? extends Update> items, Context context) {
        super(items, context);
    }

    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public Item getItem(int position) {
        return items.get(position);
    }
    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        final int height = (int) Helper.convertToDp(54);

        View rootView;
        ViewGroup contentContainer = null;
        View contentView = null;
        boolean reconvert = false;

        if(convertView!=null ) {
            rootView = convertView;
            Object tag = convertView.getTag();
            if (tag!=null && tag.equals("update")) {
                reconvert = true;
            }
        }else {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView =  inflater.inflate(R.layout.list_item_timeline_left_template,null);
        }


        Item item= getItem(position);
        if(item == null || item.isDeleted()){
            rootView.setVisibility(View.GONE);
            return rootView;
        }



        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        TimelineView timelineView = (TimelineView) rootView.findViewById(R.id.timelineView);
        timelineView.setTimelineNormal();
        if(item instanceof UpdateItem) {
            final UpdateItem updateItem = ((UpdateItem) item);
            if(reconvert){
                updateItem.reconvert(context, rootView);
            } else{
                contentView = updateItem.getViewWithOwner(context);
            }
            rootView.setTag("update");
        }else{
            contentView = item.getView(context);

            timelineView.setTimelineType(TimelineView.TIMELINE_SINGLE_BIG);

            rootView.setTag(null);
        }

        // для новых анимация, для обычных просто делаем высоту, потому что их высота по стандарту 1. скорее всего костыль
        if (item.isNew()) {

            item.setNew(false);
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setInterpolator( new AccelerateInterpolator());
            anim.setDuration(400);
            final View finalRootView = rootView;
            finalRootView.setLayoutParams(
                    new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    )
            );

            //Log.i("AGCY SPY ANIMATION", "Default height :" + height);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Float value = (Float) valueAnimator.getAnimatedValue();
                    int convertedHeight = (int) (height * (value))+1;

                    //Log.i("AGCY SPY ANIMATION", "Height :" + value);


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



        if(item instanceof DateItem){

            if(position==0){
                timelineView.setTimelineType(TimelineView.TIMELINE_START);
            }else{
                timelineView.setTimelineType(TimelineView.TIMELINE_SINGLE_BIG);
            }

            DateItem dateItem = (DateItem) item;
            // нам нужно сохранить Now и Today итемы, для дальнейшей работы с ними
            // костыль
            if(dateItem.isNow()){
                nowView = rootView;
                nowItem = dateItem;
            }else{
                if(dateItem.isToday()) {
                    todayView = rootView;
                    todayItem = dateItem;
                }
            }
        }else {
            if (position == items.size()-1) {


                timelineView.setTimelineType(TimelineView.TIMELINE_END_LONG);
                rootView.setLayoutParams(
                        new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (height*2)
                        )
                );

                if (!items.isLast(position)) {

                    timelineView.setTimelineType(TimelineView.TIMELINE_LOADING);
                    loadMore();
                }
            }
        }

        if(contentView!=null) {
            contentContainer.removeAllViews();
            contentContainer.addView(contentView);
        }
        //long elapsedTime = System.nanoTime() - startTime;
        //Log.i("AGCY SPY TIMER","Item created in "
          //      + elapsedTime / 1000000);
        return rootView;
    }


}
