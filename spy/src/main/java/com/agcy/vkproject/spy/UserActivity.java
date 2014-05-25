package com.agcy.vkproject.spy;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.ListFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tabcarousel.BackScrollManager;
import com.tabcarousel.CarouselContainer;
import com.viewpagerindicator.ContentPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.Locale;


public class UserActivity extends ActionBarActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    CarouselContainer carouselHeader;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    VKApiUserFull user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_user);


        int userid = getIntent().getExtras().getInt("id");
        user = Memory.getUserById(userid);

        ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView status = (TextView) findViewById(R.id.status_text);
        if (user.online) {
            status.setText(R.string.online);
            if (user.online_mobile)
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.online_mobile_grey, 0);
        } else {
            status.setText(Helper.getLastSeen(user));
        }
        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(), photo);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(user.first_name + " " + user.last_name);
        bar.setDisplayHomeAsUpEnabled(true);

        TabPageIndicator tabPager = (TabPageIndicator) findViewById(R.id.pager_indicator);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);


        // Resources
        final Resources res = getResources();

        // Initialize the header
        carouselHeader = (CarouselContainer) findViewById(R.id.carousel_container);

        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        tabPager.setViewPager(mViewPager);

        tabPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                carouselHeader.setSelectedTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state== ViewPager.SCROLL_STATE_IDLE){
                    carouselHeader.setMoving(false);
                }else{
                    carouselHeader.setMoving(false);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);

        MenuItem trackedButton = menu.getItem(0);
        updateTrackMenuItem(trackedButton);
        return true;
    }

    void updateTrackMenuItem(MenuItem item){

        if(user.tracked)
            item.setIcon(R.drawable.ic_tab_onlines_selected);
        else
            item.setIcon(R.drawable.ic_tab_onlines);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.track:
                Memory.setTracked(user);
                updateTrackMenuItem(item);
                break;
            case R.id.profile:
                Intent profile = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vk.com/id"+user.id));
                startActivity(profile);
                break;
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public CarouselContainer getCarousel() {
        return carouselHeader;
    }

    public VKApiUserFull getUser() {
        return user;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ContentPagerAdapter<String> {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new TypingsListFragment();
                case 0:
                default:
                    return new OnlinesListFragment();
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


    public static abstract class UpdatesListFragment extends ListFragment{


        protected CarouselContainer carouselHeader;
        protected VKApiUserFull user;

        @Override
        protected View inflateRootView(LayoutInflater inflater, ViewGroup container) {
            View view = super.inflateRootView(inflater, container);
            TextView statusView = ((TextView) view.findViewById(R.id.status));
            statusView.setTextSize(18);
            return view;
        }

        @Override
        protected void onContentBinded() {
        }

        protected abstract int getTabIndex();

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            UserActivity userActivity = ((UserActivity) getActivity());
            user = userActivity.getUser();
            carouselHeader = userActivity.getCarousel();

        }

        @Override
        public AbsListView.OnScrollListener getScrollListener() {

            return new BackScrollManager(carouselHeader, getTabIndex() );
        }

        @Override
        public View getListViewHeaderView() {
            return getActivity().getLayoutInflater().inflate(R.layout.faux_carousel_width_indicator,null);
        }

        @Override
        protected boolean hasHeader() {
            return true;
        }

    }

    public static class TypingsListFragment extends UpdatesListFragment{

        @Override
        public AbsListView.OnScrollListener getScrollListener() {

            return new BackScrollManager(carouselHeader,1 );
        }

        @Override
        public BaseAdapter adapter() {
            return new UpdatesAdapter(Memory.getTyping(user.id),context);
        }

        @Override
        protected int getTabIndex() {
            return 1;
        }
    }
    public static class OnlinesListFragment extends UpdatesListFragment {


        @Override
        public AbsListView.OnScrollListener getScrollListener() {

            return new BackScrollManager(carouselHeader,0 );
        }
        @Override
        public BaseAdapter adapter() {
            return new UpdatesAdapter(Helper.convertLastUndefinedToOnline(Memory.getOnlines(user.id)),context);
        }

        @Override
        protected int getTabIndex() {
            return 0;
        }
    }
}
