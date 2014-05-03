package com.agcy.vkproject.spy.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agcy.vkproject.spy.R;

/**
 * Created by kiolt_000 on 29-Apr-14.
 */
public class MainMenuItemView extends LinearLayout {
    public MainMenuItemView(Context context, AttributeSet set){
        super(context,set);
        init(set);
        TextView textView = new TextView(context);
        textView.setText(text);
        {

            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics()))
            );
            imageView.setImageDrawable(img);
            addView(imageView);
        }
        textView.setPadding(0,20,0,20);
        addView(textView);
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
    }
    private void init(AttributeSet attrs) {
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.MainMenuItemView);

        text = a.getString(
                R.styleable.MainMenuItemView_android_text);
        img = a.getDrawable(
                R.styleable.MainMenuItemView_image);

        //Don't forget this

        a.recycle();
    }
    private Drawable img;
    private String text;
}
