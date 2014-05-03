package com.agcy.vkproject.spy;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

import java.net.SocketException;
import java.net.UnknownHostException;

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

        SharedPreferences popupPreferences = getSharedPreferences("popup", MODE_MULTI_PROCESS);
        boolean popupStatus = popupPreferences.getBoolean("status", false);
        Button popupToggler = (Button) findViewById(R.id.popupToggler);
        Resources res = getBaseContext().getResources();
        if(popupStatus){
            popupToggler.setText("Уведомления включены");
            popupToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_green));
        }else{

            popupToggler.setText("Уведомления выключены");
            popupToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_red));
        }

        SharedPreferences longpollPreferences = getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        boolean longpolStatus = longpollPreferences.getBoolean("status", false);
        Button longpollToggler = (Button) findViewById(R.id.longpollToggler);
        if(longpolStatus){
            longpollToggler.setText("Шпион включен");
            longpollToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_green));
        }else{

            longpollToggler.setText("Шпион выключен");
            longpollToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_red));
        }


        VKUIHelper.onCreate(this);

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
                                        ((TextView)findViewById(R.id.status)).setText("Шпионить плохо -_-");
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

                                    ((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
                                    connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(Helper.START_LOADER_ID) {
                                        @Override
                                        public void onConnected() {

                                            ((TextView)findViewById(R.id.status)).setText("Подключение восстановленно");
                                            downloadData();

                                        }

                                        @Override
                                        public void onLost() {
                                            ((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
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

    public void showAll(View view) {
        startActivity(new Intent(this, AllActivity.class));
    }
    public void popupToggle(View view) {

        SharedPreferences preferences = getSharedPreferences("popup", MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        boolean status = !preferences.getBoolean("status", false);
        editor.putBoolean("status",status);
        editor.commit();
        Button popupToggler = (Button) view;
        Resources res = getBaseContext().getResources();
        if(status){
            popupToggler.setText("Уведомления включены");
            popupToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_green));
        }else{

            popupToggler.setText("Уведомления выключены");
            popupToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_red));
        }
    }

    public void longpollToggle(View view) {

        SharedPreferences preferences = getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        boolean status = !preferences.getBoolean("status", false);
        editor.putBoolean("status",status);
        editor.commit();
        Button longpollToggler = (Button) view;
        Resources res = getBaseContext().getResources();
        if(status){
            longpollToggler.setText("Шпион включен");
            longpollToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_green));
        }else{

            longpollToggler.setText("Шпион выключен");
            longpollToggler.setBackgroundDrawable(res.getDrawable(R.drawable.button_red));
        }

        Intent longPollService = new Intent(getBaseContext(), LongPollService.class);
        Bundle bundle  = new Bundle();
        bundle.putInt(LongPollService.ACTION,(status? LongPollService.ACTION_START:LongPollService.ACTION_STOP));
        longPollService.putExtras(bundle);
        startService(longPollService);

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
}
