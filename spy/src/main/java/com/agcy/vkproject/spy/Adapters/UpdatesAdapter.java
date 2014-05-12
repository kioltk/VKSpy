package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UpdatesAdapter extends BaseAdapter {

    protected final Context context;
    protected final ItemHelper.ObservableUpdatesArray items;
    protected View lastTodayView;
    protected Item lastTodayItem;
    protected boolean loading = false;

    public UpdatesAdapter(ArrayList<? extends Update> updates, Context context){
        this.items = new ItemHelper.ObservableUpdatesArray(updates);
        this.context = context;

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
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Item item = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup rootView = null;
        ViewGroup timelineContainer = null;
        ViewGroup contentContainer = null;
        View contentView = null;
        View timelineView = null;
        if (item instanceof UpdateItem) {
            // это апдейт? толгда или одна точка или две точки
            rootView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_template, null);

            if (item instanceof OnlineItem && ((OnlineItem) item).getContent().isStreak()) {
                timelineView = inflater.inflate(R.layout.list_item_timeline_double, null);
            } else
                timelineView = inflater.inflate(R.layout.list_item_timeline_single, null);


            contentView = item.getView(context);
            if(items.size()-1==position) {
                LinearLayout rootViewTemp = new LinearLayout(context);
                rootViewTemp.setOrientation(LinearLayout.VERTICAL);

                rootViewTemp.addView(rootView);
                rootView = rootViewTemp;
                if (items.isLast(position)) {
                    ViewGroup bottomContentView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_end, null);
                    rootViewTemp.addView(bottomContentView);
                }else {

                    final ViewGroup bottomContentView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_end, null);
                    rootViewTemp.addView(bottomContentView);

                    View circle = bottomContentView.findViewById(R.id.circle);

                    ScaleAnimation blinkAnimation = new ScaleAnimation(0.5f, 1, 0.5f, 1, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, 0.5f);
                    blinkAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    blinkAnimation.setDuration(750);
                    blinkAnimation.setRepeatMode(Animation.REVERSE);
                    blinkAnimation.setRepeatCount(Animation.INFINITE);
                    circle.startAnimation(blinkAnimation);
                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(loading)
                                return;
                            loading = true;
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            items.convertMore(15);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    loading = false;
                                    notifyDataSetChanged();
                                    bottomContentView.setVisibility(View.GONE);
                                }
                            });
                        }
                    })       .start()
                    ;
                }
            }
        } else {
            // это не апдейт?
            if (item instanceof DateItem) {

                rootView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_center_template, null);
                contentView = ((DateItem)item).getCenterView(context);

                ViewGroup topTimelineContainer = (ViewGroup) rootView.findViewById(R.id.topTimelineContainer);
                ViewGroup bottomTimelineContainer = (ViewGroup) rootView.findViewById(R.id.bottomTimelineContainer);
                contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
                contentContainer.addView(contentView);

                ViewGroup bottomTimelineView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_start,null);
                bottomTimelineContainer.addView(bottomTimelineView);

                if(position==0) {

                    topTimelineContainer.setVisibility(View.GONE);
                }
                else{
                    ViewGroup topTimelineView = (ViewGroup) inflater.inflate(R.layout.list_item_timeline_middle_end,null);
                    topTimelineContainer.addView(topTimelineView);
                }


                //topTimelineContainer.addView(timelineView);
                return rootView;
            }

            //timelineView =(RelativeLayout) inflater.inflate(R.layout.list_item_timeline_middle_break, null);
        }


        timelineContainer = (ViewGroup) rootView.findViewById(R.id.timelineContainer);
        contentContainer = (ViewGroup) rootView.findViewById(R.id.contentContainer);
        timelineContainer.addView(timelineView);
        contentContainer.addView(contentView);
        return rootView;

    }
    public NewItemListener newItemListener = new NewItemListener() {
        @Override
        public void newItem(Update item) {
            items.newItem(item);
            notifyDataSetChanged();
        }
    };
    public static abstract class NewItemListener {
        public abstract void newItem(Update Item);
    }

    public void removeLastToday(){
        if(lastTodayView != null){
            ValueAnimator anim = ValueAnimator.ofFloat(1f, 0f);
            anim.setDuration(700);
            anim.setInterpolator(new AccelerateInterpolator());

            final View finalRootView = lastTodayView;

            final int height = finalRootView.getMeasuredHeight();
            Log.i("AGCY SPY ANIMATION", "Default height :" + height);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Float value = (Float) valueAnimator.getAnimatedValue();
                    int convertedHeight = (int) ((height * value )+1);
                    Log.i("AGCY SPY ANIMATION", "Height :" + convertedHeight);
                    //if(finalRootView instanceof AbsListView) {
                        finalRootView.setLayoutParams(
                                new AbsListView.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        convertedHeight
                                )
                        );
                    /*}else {
                        finalRootView.setLayoutParams(
                                new RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        convertedHeight
                                )
                        );
                    }*/

                    if(value == 0){
                        if(lastTodayItem!=null) {
                            items.removeConverted(lastTodayItem);
                            lastTodayItem = null;
                            lastTodayView = null;
                            notifyDataSetChanged();
                        }
                        //callback.run();
                    }

                }

            });
            anim.start();
        }
    }

    public void notifyRecreate() {
        if (items.recreateHeaders())
            removeLastToday();


    }
}
