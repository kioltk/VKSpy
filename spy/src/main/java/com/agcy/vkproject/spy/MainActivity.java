package com.agcy.vkproject.spy;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Core.Memory;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONObject;

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
        {
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

            VKRequest request = new VKRequest("messages.getLongPollServer");
            request.executeAfterRequest(friendsRequest,new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    try {

                        JSONObject responseJson = response.json.getJSONObject("response");
                        String ts = responseJson.getString("ts");
                        String server = responseJson.getString("server");
                        String key = responseJson.getString("key");

                        SharedPreferences.Editor preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS).edit();
                        preferences.putString("server", server);
                        preferences.putString("ts", ts);
                        preferences.putString("key", key);
                        preferences.commit();
                        if(!isLongPollServiceRunning()) {
                            Intent longPollService = new Intent(getBaseContext(), LongPollService.class);
                            startService(longPollService);
                        }
                    } catch (Exception exp) {

                        Log.e("AGCY SPY LONGPOLL", exp.getMessage());

                    }
                }
            });

        }
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
        editor.putBoolean("status",!preferences.getBoolean("status",true));
        //editor.commit();
        //todo: disable longpoll
        ((TextView)view).setText((preferences.getBoolean("status", true) ? "disable" : "enable") + " longPoll");
        Toast.makeText(getBaseContext(),"not implemented =(",Toast.LENGTH_SHORT).show();

    }
}
