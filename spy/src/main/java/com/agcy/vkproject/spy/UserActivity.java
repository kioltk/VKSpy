package com.agcy.vkproject.spy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.ListFragment;
import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.viewpagerindicator.TitlePageIndicator;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.Locale;


public class UserActivity extends ActionBarActivity {

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
    Button track;
    VKApiUserFull user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        int userid = getIntent().getExtras().getInt("id");
        user = Memory.getUserById(userid);

        ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView name = (TextView) findViewById(R.id.name);
        TextView status = (TextView) findViewById(R.id.status_text);
        name.setText(user.first_name + " " + user.last_name);
        if (user.online) {
            status.setVisibility(View.VISIBLE);
            if (user.online_mobile) status.setText("С мобильного");
        }
        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(), photo);

         track = (Button) findViewById(R.id.track);
        setTracked(Memory.isTracked(user.id));
        track.setText(("Уведомления"));

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTracked(Memory.setTracked(user.id));
            }
        });

        TitlePageIndicator tabPager = (TitlePageIndicator) findViewById(R.id.pager_indicator);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabPager.setViewPager(mViewPager);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.



    }

    private void setTracked(boolean tracked){
        if (tracked) {
            track.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_green));
        } else
            track.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red));
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
            return new ListFragment((position==0?
                    new UpdatesAdapter(Helper.convertUndefined(Memory.getOnlines(user.id)), getBaseContext()):
                    new UpdatesAdapter(Memory.getTyping(user.id), getBaseContext())
            ));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.onlines).toUpperCase(l);
                case 1:
                    return getString(R.string.typings).toUpperCase(l);
            }
            return null;
        }
    }

}
