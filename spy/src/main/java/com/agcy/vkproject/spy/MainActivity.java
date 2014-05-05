package com.agcy.vkproject.spy;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.MainFragment;
import com.agcy.vkproject.spy.Fragments.OnlinesFragment;
import com.agcy.vkproject.spy.Fragments.TypingsFragment;
import com.agcy.vkproject.spy.Fragments.UsersListFragment;
import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.viewpagerindicator.ContentPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKUsersArray;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends ActionBarActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VKUIHelper.onCreate(this);
        Helper.initialize(this);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.mainIndicator);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        MainPagerAdapter mSectionsPagerAdapter  = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        indicator.setViewPager(mViewPager);



        downloadData();


    }


    private void downloadData() {

        VKParameters friendsParameters = new VKParameters();
        friendsParameters.put("order", "hints");
        friendsParameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100,online,last_seen");

        final VKRequest friendsRequest = VKApi.friends().get(friendsParameters);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                Memory.loadFriends();

                friendsRequest.executeWithListener(
                        new VKRequest.VKRequestListener() {
                            public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                            @Override
                            public void onComplete(final VKResponse response) {

                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        Memory.saveUsers((VKUsersArray) response.parsedModel);

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        //((TextView)findViewById(R.id.status)).setText("Шпионить плохо -_-");
                                    }
                                }.execute();
                                if(connectionListener!=null)
                                    connectionListener.remove();
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);
                                //unknownhost

                                if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {

                                    //((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
                                    connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(Helper.START_LOADER_ID) {
                                        @Override
                                        public void onConnected() {

                                            //((TextView)findViewById(R.id.status)).setText("Подключение восстановленно");
                                            downloadData();

                                        }

                                        @Override
                                        public void onLost() {
                                            //((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
                                        }

                                    };
                                }
                                Log.e("AGCY SPY", error.httpError.toString());
                            }
                        }
                );
                startLongpoll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Helper.initializationEnded();
            }
        }.execute();

    }
    private void startLongpoll(){

        Intent longPollService = new Intent(getBaseContext(), LongPollService.class);
        startService(longPollService);
    }
    private boolean isLongPollServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LongPollService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }


    public void showFriends(View view) {
        startActivity(new Intent(this, FriendsActivity.class));
    }





    public void logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Так мы ни за кем не последим :(")
                .setTitle("Действительно выйти?");

        builder.setPositiveButton("Выход!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Helper.DESTROYALL();
                finish();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void settings(View view) {

        startActivity(new Intent(getBaseContext(),SettingsActivity.class));
        Toast.makeText(getBaseContext(),"Ещё не сделано..", Toast.LENGTH_SHORT).show();
    }

    private class MainPagerAdapter extends FragmentPagerAdapter implements ContentPagerAdapter<Integer> {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Integer getContent(int index) {
            return R.drawable.ic_launcher;

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new MainFragment(getBaseContext());
                case 1:
                    return new TypingsFragment(getBaseContext());
                case 2:
                    return new OnlinesFragment(getBaseContext());
                default:
                    return new UsersListFragment(getBaseContext(), new UsersListFragment.OnSelectedListener() {
                        @Override
                        public void onSelect(VKApiUser user) {

                            Intent intent = new Intent(getBaseContext(), UserActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("id", user.id);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });

            }


        }
    }
}
