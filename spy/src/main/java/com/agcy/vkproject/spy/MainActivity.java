package com.agcy.vkproject.spy;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

public class MainActivity extends Activity {


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

            if(isNetworkConnected()){
                downloadData();
            }else
            {

                ((TextView)findViewById(R.id.status)).setText("Нет интрнта");
                new NetworkStateReceiver.NetworkStateChangeListener() {
                    @Override
                    public void onConnected() {

                        ((TextView)findViewById(R.id.status)).setText("о! есть интрнт\nзгрзка");
                        downloadData();
                    }
                };
            }
    }


    private void downloadData(){

        VKParameters friendsParameters = new VKParameters();
        friendsParameters.put("order", "hints");
        friendsParameters.put("fields", "sex,photo_100,online,last_seen");

        VKRequest friendsRequest = VKApi.friends().get(friendsParameters);
        friendsRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                showFriends((VKUsersArray) response.parsedModel);
            }
        });
        startLongpoll();
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


    public void showFriends(VKUsersArray friends) {
        Memory.saveFriends(friends);
        findViewById(R.id.displayButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.status).setVisibility(View.GONE);
    }

    public void showFriends(View view) {
        startActivity(new Intent(this, FriendsActivity.class));
    }

    public void showAll(View view) {
        startActivity(new Intent(this, AllActivity.class));
    }
    public void popupToggle(View view) {

        SharedPreferences preferences = getSharedPreferences("popup", MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("status",!preferences.getBoolean("status",true));
        editor.commit();
        ((TextView)view).setText((preferences.getBoolean("status",true)?"disable":"enable")+" popup");
    }

    public void longpollToggle(View view) {


        SharedPreferences preferences = getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        Boolean status = !preferences.getBoolean("status", true);
        editor.putBoolean("status",status);

        Intent longPollService = new Intent(getBaseContext(), LongPollService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(LongPollService.ACTION, (status? LongPollService.ACTION_START: LongPollService.ACTION_STOP));
        longPollService.putExtras(bundle);
        startService(longPollService);

        editor.commit();

        ((TextView)view).setText((status ? "disable" : "enable") + " longPoll");

    }
}
