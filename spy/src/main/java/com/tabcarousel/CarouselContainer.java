/*
 * Copyright (C) 2013 Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tabcarousel;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.agcy.vkproject.spy.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * This is a horizontally scrolling carousel with 2 tabs.
 */
public class CarouselContainer extends RelativeLayout {


    /**
     * Used to determine is the carousel is animating
     */
    private boolean mTabCarouselIsAnimating;

    /**
     * Allowed horizontal scroll length
     */

    /**
     * Allowed vertical scroll length
     */
    private int mAllowedVerticalScrollLength = Integer.MIN_VALUE;

    private float[] storedYCoordinate = new float[2];
    private int selectedTab;

    /**
     * @param context The {@link android.content.Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public CarouselContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Add the onTouchListener
        // Retrieve the carousel dimensions
        final Resources res = getResources();

        // Height of the indicator
        mAllowedVerticalScrollLength = res.getDimensionPixelSize(R.dimen.carousel_container_height) ;
    }







    public boolean isTabCarouselIsAnimating() {
        return mTabCarouselIsAnimating;
    }
    public void restoreYCoordinate(int currentTab) {
        restoreYCoordinate(150,currentTab);
    }

    public void restoreYCoordinate(int duration, int currentTab) {

        if(currentTab!=selectedTab)
            return;


        final float storedYCoordinate = getStoredYCoordinate(currentTab);

        final Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.anim.accelerate_decelerate_interpolator);

        final ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", storedYCoordinate);
        animator.addListener(mTabCarouselAnimatorListener);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.start();
    }

    public void moveToYCoordinate(float y, int currentTab) {
        storeYCoordinate(y,currentTab);
        restoreYCoordinate(0,currentTab);
    }

    private void storeYCoordinate(float y, int tab) {
        storedYCoordinate[tab] = y;
    }


    public int getAllowedVerticalScrollLength() {
        return mAllowedVerticalScrollLength;
    }


    /**
     * This listener keeps track of whether the tab carousel animation is
     * currently going on or not, in order to prevent other simultaneous changes
     * to the Y position of the tab carousel which can cause flicker.
     */
    private final AnimatorListener mTabCarouselAnimatorListener = new AnimatorListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            mTabCarouselIsAnimating = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationStart(Animator animation) {
            mTabCarouselIsAnimating = true;
        }
    };

    public void moveToTop(int currentTab) {
        storeYCoordinate(-getAllowedVerticalScrollLength(),currentTab);
        restoreYCoordinate(0, currentTab);
    }

    public float getStoredYCoordinate(int tab) {
        return storedYCoordinate[tab];
    }


    public void setSelectedTab(int currentTab) {
        this.selectedTab = currentTab;
        restoreYCoordinate(currentTab);
    }

    public void setMoving(boolean moving) {
        mTabCarouselIsAnimating = moving;
    }
}
