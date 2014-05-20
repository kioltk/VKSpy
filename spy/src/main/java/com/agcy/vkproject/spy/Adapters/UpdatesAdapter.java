package com.agcy.vkproject.spy.Adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.UpdateItem;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UpdatesAdapter extends BaseAdapter {

    protected final Context context;
    protected final ItemHelper.ObservableUpdatesArray items;
    protected boolean loading = false;
    protected View nowView;
    protected DateItem nowItem;
    protected View todayView;
    protected DateItem todayItem;

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

                    ScaleAnimation blinkAnimation = new ScaleAnimation(0.5f, 1, 0.5f, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, 0.5f);
                    blinkAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    blinkAnimation.setDuration(750);
                    blinkAnimation.setRepeatMode(Animation.REVERSE);
                    blinkAnimation.setRepeatCount(Animation.INFINITE);
                    circle.startAnimation(blinkAnimation);
                    final Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (loading)
                                return;
                            loading = true;
                            loadMore();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    loading = false;
                                    notifyDataSetChanged();
                                    bottomContentView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
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


                DateItem dateItem = (DateItem) item;

                if(dateItem.isNow()){
                    nowView = rootView;
                    nowItem = dateItem;
                }else{
                    if(dateItem.isToday()) {
                        todayView = rootView;
                        todayItem = dateItem;
                    }
                }

                if(position==0) {
                    // нам нужно сохранить Now и Today итемы, для дальнейшей работы с ними
                    // костыль


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

    public void pauseNew() {
        items.disableNewAnimation();
    }

    public void resumeNew() {

        items.enableNewAnimation();
    }

    protected void loadMore() {

        Log.i("AGCY SPY","Load more");
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(loading)
                    return;

                loading = true;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                items.convertMore(100);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loading = false;
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public static abstract class NewItemListener {
        public abstract void newItem(Update Item);
    }


    public void recreateHeaders() {
        Log.i("AGCY SPY","Notified to recreate headers");
        Boolean recreateItemsStatus = items.recreateHeaders();

        if(recreateItemsStatus) {

            if(todayItem!=null && todayView!=null) {
                todayItem.setDeleted();

                final View dismissView = todayView;
                final Item dismissItem = todayItem;
                final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                final int originalHeight = dismissView.getHeight();

                items.removeConverted(dismissItem);
                ValueAnimator animator = ValueAnimator.ofInt(originalHeight,1).setDuration(1000);
                animator.setInterpolator(new DecelerateInterpolator());

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                        lp.height = (Integer) valueAnimator.getAnimatedValue();
                        dismissView.setLayoutParams(lp);
                        //Log.i("AGCY SPY ANIMATION","Current height: "+valueAnimator.getAnimatedValue()+" item's height: "+dismissView.getHeight()+" min height"+ dismissView.getMinimumHeight());
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        todayItem = nowItem;
                        todayView = nowView;

                        nowItem = null;
                        nowView = null;

                        notifyDataSetChanged();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                animator.start();

                //notifyDataSetChanged();
            }


        }
    }
}
