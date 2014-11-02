package com.LDroid.Animations;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Created by kioltk on 7/24/14.
 */
public class SlideOutAnimation extends AnimationSet {
    public SlideOutAnimation() {
        super(true);
        setInterpolator(new DecelerateInterpolator(0.8f));
        AlphaAnimation fadeInAnimation = new AlphaAnimation(1,0);
        TranslateAnimation translateAnimation  = new TranslateAnimation(0,-100,0,0);
        addAnimation(translateAnimation);
        addAnimation(fadeInAnimation);
        setDuration(400);
    }
}
