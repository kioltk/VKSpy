package com.happysanta.spy;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.Core.Memory;
import com.happysanta.spy.Core.Notificator;
import com.happysanta.spy.Core.UberFunktion;
import com.happysanta.spy.Fragments.MainFragment;
import com.happysanta.spy.Fragments.OnlinesFragment;
import com.happysanta.spy.Fragments.TypingsFragment;
import com.happysanta.spy.Fragments.UsersListFragment;
import com.happysanta.spy.Longpoll.LongPollService;
import com.happysanta.spy.Receivers.NetworkStateReceiver;
import com.viewpagerindicator.ContentPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONException;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends ActionBarActivity {

    public static final int ONLINES = 1;
    public static boolean isOpened = false;
    private OnlinesFragment onlinesFragment;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOpened = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Notificator.clearNotifications();
        VKUIHelper.onResume(this);
        isOpened = true;
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
        if(!Helper.isInitialized())
            Helper.initialize(this);
        Helper.setMainActivity(this);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.mainIndicator);

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        final MainPagerAdapter mSectionsPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        indicator.setViewPager(mViewPager);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("AGCY SPY", "Page selected: " + position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if(savedInstanceState==null) {
            downloadData();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                int page = bundle.getInt("page", 0);
                mViewPager.setCurrentItem(page);
            }
        }

    }


    private void downloadData() {
        // всё сложно..
        final Handler handler = new Handler();

        VKParameters friendsParameters = new VKParameters();
        friendsParameters.put("order", "hints");
        friendsParameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100");

        final VKRequest friendsRequest = VKApi.friends().get(friendsParameters);

        // начинаем поток, который загружает всех с бд. друзей может быть много, поэтому нужен фон
        // когда загрузились, уведомляем всех, что загрузились. все сразу начинают юзать данные
        // после чего скачиваем новый список друзей, и заливаем его в бд. По итогу обновляем
        // списки, уведомляем, что закачка завершена.
        // если какие-то друзья загрузились из бд, значит мы можем за ними следить => включаем лонпгол
        // иначе ждём, пока они загрузятся

        new Thread(new Runnable() {
            @Override
            public void run() {
                Memory.loadUsers();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Helper.loadingEnded();
                    }
                });
                friendsRequest.executeWithListener(
                        new VKRequest.VKRequestListener() {

                            public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                            @Override
                            public void onComplete(final VKResponse response) {
                                // асинхронно сохраняем, чтобы никого не задеть...
                                // мы ведь внутри обычного потока
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean firstLoading = false;
                                        if(Memory.users.isEmpty()){
                                            firstLoading = true;
                                        }
                                        VKUsersArray friends = (VKUsersArray) response.parsedModel;
                                                /*
                                                final VKUsersArray friendsArrayCopy = new VKUsersArray();
                                                for (VKApiUserFull vkApiUserFull : friends) {
                                                    friendsArrayCopy.add(vkApiUserFull);
                                                }
                                                Helper.fetchOnlines(friends,0);
                                                */
                                        Memory.saveFriends(friends);

                                        final boolean finalFirstLoading = firstLoading;

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(Memory.users.getById(1)!=null){
                                                    UberFunktion.initializeBackground(getBaseContext());
                                                }
                                                if(finalFirstLoading){
                                                    Helper.trackedUpdated();
                                                }
                                                Helper.downloadingEnded();
                                                startLongpoll();
                                            }
                                        });
                                    }
                                }).start();

                                if (connectionListener != null)
                                    connectionListener.remove();
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);

                                switch (error.errorCode) {
                                    case VKError.VK_API_REQUEST_HTTP_FAILED:
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
                                        break;
                                    default:
                                        Log.e("AGCY SPY", error.toString());
                                        break;
                                }
                            }

                        }
                );

                VKParameters userParameters = new VKParameters();
                try {
                    PackageInfo thisPackage = getPackageManager().getPackageInfo(getPackageName(), 0);
                    userParameters.put("versionCode",thisPackage.versionCode);
                    userParameters.put("versionName",thisPackage.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                new VKRequest("execute.getUser", userParameters).executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            VKApiUserFull user = new VKList<VKApiUserFull>(response.json.getJSONArray("response"), VKApiUserFull.class).get(0);
                            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_MULTI_PROCESS).edit();
                            editor.putString("name", user.first_name + " " + user.last_name);
                            editor.putString("photo", user.getBiggestPhoto());
                            editor.putInt("id", user.id);
                            editor.commit();

                        } catch (Exception exp) {
                            Context baseContext = MainActivity.this;
                            AlertDialog.Builder successDialogBuilder = new AlertDialog.Builder(baseContext);
                            successDialogBuilder.setCancelable(true);


                            successDialogBuilder.setTitle((R.string.important_notification));
                            TextView notifcationView = new TextView(baseContext);
                            notifcationView.setTextColor(0xff333333);
                            notifcationView.setTextSize(16);
                            notifcationView.setPadding(0, Helper.convertToDp(20), 0, Helper.convertToDp(20));
                            notifcationView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            notifcationView.setGravity(Gravity.CENTER);
                            try {
                                notifcationView.setText(Html.fromHtml(response.json.getString("response")));
                            } catch (JSONException e) {
                                return;
                            }
                            notifcationView.setMovementMethod(LinkMovementMethod.getInstance());

                            successDialogBuilder.setView(notifcationView);
                            successDialogBuilder.setPositiveButton(baseContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            successDialogBuilder.show();
                        }
                    }
                });
            }
        }).start();
    }

    private void startLongpoll(){

        Intent longPollService = new Intent(getBaseContext(), LongPollService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(LongPollService.ACTION,LongPollService.ACTION_START);
        longPollService.putExtras(bundle);
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

    public void filterOnlines(View view) {
        startActivity(new Intent(this, TestFilterActivity.class));
    }
    public void filterTypings(View view){
        Toast.makeText(getBaseContext(),".!.",Toast.LENGTH_SHORT).show();
    }
    private class MainPagerAdapter extends FragmentPagerAdapter implements ContentPagerAdapter<Integer> {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Integer getContent(int index) {
            switch (index) {
                case 0:
                    return R.drawable.tab_typings;
                case 1:
                    return R.drawable.tab_onlines;
                case 2:
                    return R.drawable.tab_friends;
                default:
                    return R.drawable.tab_vkpsy;
            }

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new TypingsFragment();
                case 1:
                    onlinesFragment = new OnlinesFragment();
                    return onlinesFragment;
                case 2:
                    UsersListFragment fragment = new UsersListFragment();
                    Memory.addUsersListener(fragment.getListener());
                    return fragment;
                default:
                    return new MainFragment();

            }


        }

    }


}
