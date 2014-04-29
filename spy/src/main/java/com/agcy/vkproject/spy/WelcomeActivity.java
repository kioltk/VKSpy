package com.agcy.vkproject.spy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

import java.util.Locale;


public class WelcomeActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);



        SharedPreferences preferences = getSharedPreferences("start", MODE_MULTI_PROCESS);
        if(!preferences.getBoolean("firstStart",true)) {
            startActivity(new Intent(getBaseContext(),StartActivity.class));
            finish();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }




    

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return new PlaceholderFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public PlaceholderFragment() {
        }
        View rootView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            showSanta();
            return rootView;
        }

        private void hideSanta() {
            Animation translate = new TranslateAnimation(0,0,0,400);
            translate.setInterpolator(new AccelerateInterpolator(1.6f));
            AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
            alphaAnimation.setInterpolator(new AccelerateInterpolator());

            AnimationSet set = new AnimationSet(false);
            set.addAnimation(alphaAnimation);
            set.addAnimation(translate);
            set.setDuration(1000);
            set.setStartOffset(100);

            final View santa =  rootView.findViewById(R.id.image);

            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    santa.setVisibility(View.GONE);
                    showStart();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            santa.startAnimation(set);
        }


        private void showSanta() {
            Animation translate = new TranslateAnimation(0,0,-400,0);
            translate.setInterpolator(new OvershootInterpolator(1.6f));
            AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
            alphaAnimation.setInterpolator(new AccelerateInterpolator());

            AnimationSet set = new AnimationSet(false);
            set.addAnimation(alphaAnimation);
            set.addAnimation(translate);
            set.setDuration(1000);
            set.setStartOffset(300);
            final View santa =  rootView.findViewById(R.id.image);

            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    santa.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    hideSanta();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            santa.startAnimation(set);
        }
        private void showStart() {

            AlphaAnimation show = new AlphaAnimation(0,1);
            show.setInterpolator(new AccelerateInterpolator());
            show.setDuration(1000);

            View text = rootView.findViewById(R.id.text);
            Button start = (Button) rootView.findViewById(R.id.start);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WelcomeActivity.this, StartActivity.class));

                    SharedPreferences preferences = getSharedPreferences("start", MODE_MULTI_PROCESS);
                    preferences.edit().putBoolean("firstStart",false).commit();

                    finish();
                }
            });
            text.setVisibility(View.VISIBLE);
            start.setVisibility(View.VISIBLE);
            start.startAnimation(show);
            text.startAnimation(show);
        }
    }

}
