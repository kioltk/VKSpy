package com.happysanta.spy.Views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.R;

/**
 * Created by kiolt_000 on 19-May-14.
 */
public class TimelineView extends RelativeLayout {

    public final static int CIRCLE_COUNT_SINGLE = 1;


    public final static int CIRCLE_SIZE_BIG = 2;
    public static final int TIMELINE_SIMGPLE = 0;
    public static final int TIMELINE_START = 1;
    public static final int TIMELINE_END_LONG = 2;
    public static final int TIMELINE_SINGLE_BIG = 3;
    public static final int TIMELINE_LOADING = 4;


    private int circlesCount = 0;
    private int circlesSize = 0;
    private int timelineType = 0;

    int dp2 = 4;
    int dp54 = 108;
    private View circle;
    View timelineView;

    public TimelineView(Context context, AttributeSet attrs) {
        super(context,attrs);

        if(this.isInTouchMode ()){
            dp2 = Helper.convertToDp(2);
            dp54 = Helper.convertToDp(54);
        }


        Resources res = context.getResources();

        timelineView = new View(context);
        timelineView.setBackgroundColor(res.getColor(R.color.blue_timeline));

        setTimelineNormal();


        circle = new View(context);
        circle.setBackgroundDrawable(res.getDrawable(R.drawable.circle));
        setCircleNormal();

        addView(timelineView);
        addView(circle);

    }




    public View getCircle() {
        return circle;
    }

    public void setTimelineType(int timelineType) {


        circle.clearAnimation();


        setCircleNormal();
        setTimelineNormal();

        switch (timelineType) {
            case TIMELINE_START:
                setTimelineHalf();
                alignTimelineToBottom();

                setCircleBig();
                break;
            case TIMELINE_SINGLE_BIG:
                setCircleBig();
                break;
            case TIMELINE_LOADING:
                loading();
            case TIMELINE_END_LONG:
                setTimelineOneAndHalf();
                alignTimelineToTop();

                setCircleBig();
                alignCircleToBottom();
                break;
            default:
                return;
        }
        this.timelineType = timelineType;

    }

    public void alignCircleToBottom() {
        LayoutParams circleParams = (LayoutParams) circle.getLayoutParams();

        circleParams.addRule(ALIGN_PARENT_BOTTOM);
        circleParams.setMargins(0, 0, 0, (int) (dp54/2-circleParams.height/2));
        circle.setLayoutParams(circleParams);
    }

    private void alignTimelineToTop() {
        LayoutParams timelineParams = (LayoutParams) timelineView.getLayoutParams();
        timelineParams.addRule(ALIGN_PARENT_TOP);
        timelineView.setLayoutParams(timelineParams);
    }

    public void alignTimelineToBottom() {
        LayoutParams timelineParams = (LayoutParams) timelineView.getLayoutParams();
        timelineParams.addRule(ALIGN_PARENT_BOTTOM);
        timelineView.setLayoutParams(timelineParams);
    }

    public void setTimelineOneAndHalf() {
        LayoutParams timelineParams = (LayoutParams) timelineView.getLayoutParams();
        timelineParams.height = (int) (dp54 *1.5);
        //timelineParams.setMargins(0,0,0, (int) (dp54*0.5));
        timelineView.setLayoutParams(timelineParams);
    }

    public void setTimelineHalf() {
        LayoutParams timelineParams = (LayoutParams) timelineView.getLayoutParams();
        timelineParams.height = dp54 / 2;
        timelineView.setLayoutParams(timelineParams);
    }
    public void setTimelineNormal() {
        LayoutParams timelineParams = new LayoutParams(dp2, dp54);
        timelineParams.addRule(CENTER_IN_PARENT);
        setMinimumHeight(dp54);
        timelineView.setLayoutParams(timelineParams);
    }

    public void setCircleNormal() {
        int circleSize = (int) (dp2 * 3.5);
        LayoutParams circleParams = new LayoutParams(circleSize, circleSize);
        circleParams.addRule(CENTER_IN_PARENT);
        circle.setLayoutParams(circleParams);
    }
    void setCircleBig() {
        LayoutParams circleParams = (LayoutParams) circle.getLayoutParams();
        circleParams.height = (dp2 * 6);
        circleParams.width = (dp2 * 6);
        circle.setLayoutParams(circleParams);
    }
    void loading(){

        ScaleAnimation blinkAnimation = new ScaleAnimation(0.5f, 1, 0.5f, 1, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, 0.5f);
        blinkAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        blinkAnimation.setDuration(750);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(Animation.INFINITE);
        circle.startAnimation(blinkAnimation);

    }
}
