package com.happysanta.crazytyping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.happysanta.crazytyping.Core.Helper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;

import java.util.Locale;


public class WelcomeActivity extends ActionBarActivity {

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        VKUIHelper.onCreate(this);

        Helper.initialize(this);
        // https://oauth.vk.com/authorize?client_id=4420354&api_version=5.2&client_secret=Y3aRxrk6Hvqeo7P9wnxT&response_type=token&revoke=1&scope=friends,messages,offline
        VKAccessToken token = VKAccessToken.tokenFromUrlString("access_token=516b35b619de66cd5723b08a542f2808d801bbcc173bebc0227e1eacf6e5d9272d44774996e1e216595fa&expires_in=0&user_id=32018303");
        VKSdk.setAccessToken(token, false);
        if (VKSdk.wakeUpSession()) {

            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
            return;

        }



        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);




    }

    private void login(){

        com.happysanta.crazytyping.Core.VKSdk.authorizeFirst(this);
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
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return "hello!";
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        View rootView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            rootView.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    ((WelcomeActivity) getActivity()).login();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.login_title);
                    builder.setMessage(R.string.login_message);
                    builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });//.show();

                }
            });
            rootView.findViewById(R.id.privacy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), InfoActivity.class);
                    intent.putExtra("content",R.layout.privacyandpolicty);
                    intent.putExtra("title",R.string.termsandpolicy);
                    startActivity(intent);

                }
            });
            return rootView;
        }
    }

}
