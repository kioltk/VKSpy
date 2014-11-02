package com.LDroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.LDroid.Animations.SlideInAnimation;
import com.LDroid.Animations.SlideOutAnimation;
import com.happysanta.crazytyping.MainActivity;
import com.happysanta.crazytyping.R;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class LActionBar extends RelativeLayout {
    private String title = "Title"; // TODO: use a default from R.string...
    private int backgroundColor = 0xff5677fc; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable icon = null;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private LinearLayout extended;

    public LActionBar(Context context) {
        super(context);
        //init(null, 0);
    }

    public LActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        init(attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LinearLayout abContent = new LinearLayout(context);
        abContent.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams abContentLayoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.action_bar_default_height));
        abContent.setLayoutParams(abContentLayoutParams);
        abContent.setGravity(Gravity.CENTER_VERTICAL);
        abContent.setPadding((int) getResources().getDimension(R.dimen.action_bar_padding),0,0,0);

        TextView titleView = new TextView(context);
        titleView.setText("hello");
        titleView.setTextColor(getResources().getColor(R.color.white));
        if(icon != null)
            titleView.setPadding((int) getResources().getDimension(R.dimen.action_bar_padding),0,0,0);

        abContent.addView(titleView);

        addView(abContent);
        abContent.setId(R.id.action_bar_content);
        extended = new LinearLayout(context);
        extended.setOrientation(LinearLayout.VERTICAL);
        LayoutParams extendedParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        extendedParams.addRule(BELOW,R.id.action_bar_content);
        extended.setLayoutParams(extendedParams);
        addView(extended);
    }

    public void openActionBar(final ArrayList<View> views) {
        final int currentHeight = extended.getHeight();


        int extendedHeight = (int) getResources().getDimension(R.dimen.action_bar_extended);

        MainActivity.ResizeAnimation resizeAnimation = new MainActivity.ResizeAnimation(extended,
                extended.getWidth(), currentHeight,
                extended.getWidth(), extendedHeight){
            @Override
            public void firstUpdate() {

                System.out.println(extended.getHeight() + "\n" +
                        extended.getMeasuredHeight());
            }
        };

        resizeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                System.out.println(extended.getHeight());
                for (int i = 0; i < views.size(); i++) {
                    View view = views.get(i);

                    extended.addView(view);
                    final int finalI = i;
                    view.startAnimation(new SlideInAnimation(){{setStartOffset(finalI *20);}});
                }
                System.out.println(extended.getHeight() + "\n" +
                        extended.getMeasuredHeight());
            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        resizeAnimation.setInterpolator(new AccelerateInterpolator());
        //resizeAnimation.start();
        extended.startAnimation(resizeAnimation);


    }
    public void closeActionBar(){
        clear();

    }
    public void clear(){


        for (int i = 0; i < extended.getChildCount(); i++) {
            final View view = extended.getChildAt(i);

            final int finalI = i;
            view.startAnimation(new SlideOutAnimation() {{
                setStartOffset(finalI * 20);
                setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        extended.post(new Runnable() {
                            @Override
                            public void run() {
                                extended.removeView(view);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }});
        }
    }
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LActionBar, defStyle, 0);
        if (a.hasValue(R.styleable.LActionBar_barTitle))
            title = a.getString(
                    R.styleable.LActionBar_barTitle);
        backgroundColor = a.getColor(
                R.styleable.LActionBar_backgroundColor,
                backgroundColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.

        if (a.hasValue(R.styleable.LActionBar_barIcon)) {
            icon = a.getDrawable(
                    R.styleable.LActionBar_barIcon);
            icon.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        setBackgroundColor(backgroundColor);
        int padding = (int) getResources().getDimension(R.dimen.action_bar_padding);
        setPadding(padding, getPaddingTop() + padding/2, padding, padding);

        // Update TextPaint and text measurements from attributes
    }

}
