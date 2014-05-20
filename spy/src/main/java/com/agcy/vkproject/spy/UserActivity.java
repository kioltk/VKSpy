package com.agcy.vkproject.spy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.ListFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.viewpagerindicator.ContentPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;
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
    VKApiUserFull user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);




        int userid = getIntent().getExtras().getInt("id");
        user = Memory.getUserById(userid);

        ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView status = (TextView) findViewById(R.id.status_text);
        if (user.online) {
            status.setText(R.string.online);
            if (user.online_mobile)
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.online_mobile_grey, 0);
        }else{
            status.setText(Helper.getLastSeen(user));
        }
        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(), photo);



        TabPageIndicator tabPager = (TabPageIndicator) findViewById(R.id.pager_indicator);
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

        ActionBar bar = getSupportActionBar();
        bar.setTitle(user.first_name+" "+ user.last_name);
        bar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ContentPagerAdapter<String> {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new ListFragment() {
                        @Override
                        public BaseAdapter adapter() {
                            return new UpdatesAdapter(Helper.convertLastUndefinedToOnline(Memory.getOnlines(user.id)),context);
                        }
                    };
                default:
                    return new ListFragment() {
                        @Override
                        public BaseAdapter adapter() {
                            return new UpdatesAdapter(Memory.getTyping(user.id),context);
                        }
                    };

            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public String getContent(int position) {
            return String.valueOf(getPageTitle(position));
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
