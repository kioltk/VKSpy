package com.agcy.vkproject.spy.Longpoll;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class LongPollService extends Service {

    public static final int LONGPOLL_REFRESHER_ID=27;
    public static final int LONGPOLL_CONNECTION_ID = 28;
    public static final String ACTION = "ACTION";
    public static final int ACTION_START_SAFE = 2;
    public static final int ACTION_START = 1;
    public static final int ACTION_STOP =-1;

    private String key;
    private String ts;
    private String server;

    private LongPollConnection connection;
    private static NetworkStateReceiver.NetworkStateChangeListener networkStateChangeListener;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(networkStateChangeListener==null) {
            new NetworkStateReceiver.NetworkStateChangeListener(LONGPOLL_CONNECTION_ID) {
                @Override
                public void onConnected() {
                    startLongpoll();
                }

                @Override
                public void onLost() {
                    connection.cancel(true);
                }

            };
        }
        if(intent==null){
            //onRestart

            Log.i("AGCY SPY LONGPOLLSERVICE","Intent equals null" );
            startSafe();

        }
        else{

            // startService
            Bundle bundle = intent.getExtras();
            if (bundle!=null) {
                int action = bundle.getInt(ACTION, 0);
                switch (action) {
                    case ACTION_START:
                        Log.i("AGCY SPY LONGPOLLSERVICE","force start " );
                        restoreSettings();
                        startLongpoll();

                        break;
                    case ACTION_START_SAFE:

                        Log.i("AGCY SPY LONGPOLLSERVICE","safe start " );
                        startSafe();
                        break;
                    case ACTION_STOP:
                        if (connection != null) {

                            Log.i("AGCY SPY LONGPOLLSERVICE","stop " );
                            connection.cancel(false);
                            connection = null;
                            saveSettings();
                            return Service.START_NOT_STICKY;
                        }
                        break;
                }
            }else{
                Log.i("AGCY SPY LONGPOLLSERVICE","simple call" );
                if(connection==null || connection.isFinished())
                    refreshSettings();

            }

            // any other start

        }

        return Service.START_STICKY;

    }

    private void startSafe() {

        Helper.initialize(getApplicationContext());

        com.agcy.vkproject.spy.Core.VKSdk.initializeBackground(getApplicationContext());

        //refreshSettings();
        restoreSettings();
        startLongpoll();
        Log.i("AGCY SPY LONGPOLLSERVICE", "Started safe" + " key: " + key);
    }

    @Override
    public void onCreate() {

        // berore start
        // only on first startService and restart




        super.onCreate();
        Log.i("AGCY SPY LONGPOLLSERVICE", "created"+" key: " + key );

    }
    @Override
    public void onDestroy() {
        Log.w("AGCY SPY LONGPOLLSERVICE","destroyed"+" key: " + key );
        connection.cancel(true);
        saveSettings();
    }

    private void saveSettings() {

        SharedPreferences.Editor preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS).edit();
        preferences.putString("server", server);
        preferences.putString("ts", ts);
        preferences.putString("key", key);
        preferences.commit();

    }
    private void refreshSettings() {



        Log.w("AGCY SPY LONGPOLLSERVICE","refreshing..");
        VKRequest request = new VKRequest("messages.getLongPollServer");
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onError(VKError error) {


                if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {
                    new NetworkStateReceiver.NetworkStateChangeListener(LONGPOLL_REFRESHER_ID) {
                        @Override
                        public void onConnected() {
                            refreshSettings();
                        }

                        @Override
                        public void onLost() {

                        }
                    };
                }
                Log.w("AGCY SPY LONGPOLLSERVICE","refreshing error" + error);
            }

            @Override
            public void onComplete(VKResponse response) {

                try {

                    JSONObject responseJson = response.json.getJSONObject("response");
                    ts = responseJson.getString("ts");
                    server = responseJson.getString("server");
                    key = responseJson.getString("key");

                    Log.w("AGCY SPY LONGPOLLSERVICE","refreshing complete");

                    saveSettings();

                    startLongpoll();

                } catch (Exception exp) {
                    Log.e("AGCY SPY LONGPOLL", "refreshing error" + exp.getMessage());

                }
            }
        });

        Log.w("AGCY SPY LONGPOLLSERVICE","refreshing executed ");
    }
    private void restoreSettings(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll",MODE_MULTI_PROCESS);
        server = preferences.getString("server","");
        ts = preferences.getString("ts","");
        key = preferences.getString("key","");

    }

    private void startLongpoll() {

        if(connection!=null){

            Log.w("AGCY SPY LONPOLL", "startLongpoll called, but there is another longpoll");
            if(!connection.isFinished()){
                connection.cancel(true);

                Log.wtf("AGCY SPY LONPOLL", "KILL IT!!1");
            }
        }

        connection = new LongPollConnection(server, key, ts) {
            @Override
            public void onSuccess(JSONArray updatesJson, String ts) {

                LongPollService.this.ts = ts;
                saveSettings();
                ArrayList<Update> updates = new ArrayList<Update>();
                for (int i = 0; i < updatesJson.length(); i++) {
                    try {
                        String[] updateJson = updatesJson.getString(i)
                                .replace("[", "")
                                .replace("]", "")
                                .split(",");
                        if (shouldHandle(updateJson))
                            updates.add(new Update(updateJson));

                    } catch (Exception exp) {
                        Log.e("AGCY SPY LONGPOLL", "update parsing error: " + exp.toString());
                    }
                }
                Helper.newUpdates(updates);
                startLongpoll();
                saveLongpollExecuted();
            }

            @Override
            public void onError(Exception exp) {

                Log.e("AGCY SPY LONGPOLL", exp.toString() + "" + exp.getMessage());
                if (exp instanceof JSONException) {
                    refreshSettings();
                }
            }


        };
        if (checkLongpollEnabled()) {
            connection.execute();
            Log.i("AGCY SPY", "Longpoll executed");
        }else{

            Log.i("AGCY SPY", "Longpoll disabled");
        }
    }
    private Boolean checkLongpollEnabled(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        return preferences.getBoolean("status",true);
    }
    private void saveLongpollExecuted(){
        SharedPreferences preferences = getBaseContext().getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastUpdate", (int) Helper.unixNow());
        editor.commit();

    }
    public static Boolean shouldHandle(String[] updateArray) {

        int updateType = Integer.valueOf(updateArray[0]);
        switch (updateType) {
            case Update.TYPE_ONLINE:
            case Update.TYPE_OFFLINE:
            case Update.TYPE_USER_TYPING:
            case Update.TYPE_CHAT_TYPING:
                return true;
            case Update.TYPE_MESSAGE:
                int flags = Integer.valueOf(updateArray[2]);
                if ((flags & Update.FLAG_MESSAGE_CHAT) == Update.FLAG_MESSAGE_CHAT) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }


    public class Update {
        public static final int TYPE_MESSAGE = 4;
        public static final int TYPE_MESSAGE_CHAT = 400;

        public static final int TYPE_ONLINE = 8;
        public static final int TYPE_OFFLINE = 9;
        public static final int TYPE_USER_TYPING = 61;
        public static final int TYPE_CHAT_TYPING = 620;// rebuild architecture to get chats also

        public static final int FLAG_MESSAGE_CHAT = 8192;

        private final int updateType;
        private final int flags;
        private final int userId;

        public Update(String[] updateArray){

            int tempUpdateType = Integer.valueOf(updateArray[0]);
            if(tempUpdateType==TYPE_MESSAGE) {

                this.userId = Integer.valueOf(updateArray[3]);
                this.flags = Integer.valueOf(updateArray[2]);
                if((flags & FLAG_MESSAGE_CHAT) == FLAG_MESSAGE_CHAT){
                    tempUpdateType = TYPE_MESSAGE_CHAT;
                }
            }else {
                this.userId = Integer.valueOf(updateArray[1]) * (tempUpdateType < 10 ? -1 : 1);
                this.flags = Integer.valueOf(updateArray[2]);
            }
            updateType = tempUpdateType;
        }

        public int getType(){return updateType;}
        public String getHeader(){
            return getUser().first_name+" "+getUser().last_name;
        }
        public String getMessage(){
            VKApiUserFull user = getUser();
            switch (updateType) {
                case Update.TYPE_MESSAGE:
                    return "Новое сообщение";
                case Update.TYPE_ONLINE:
                    return (user.sex ==2?"Зашёл в сеть":"Зашла в сеть" ) ;
                case Update.TYPE_OFFLINE:
                    return (user.sex ==2?"Вышел из сети":"Вышла из сети" ) ;
                case Update.TYPE_USER_TYPING:
                    return (user.sex ==2?"Писал 3 минуты назад":"Писала 3 минуты назад" ) ;
                case Update.TYPE_CHAT_TYPING:
                    return (user.sex ==2?"Писал в беседе 3 минуты назад":"Писала в беседе 3 минуты назад" ) ;
            }
            return "Уведомление";
        }
        public String getImageUrl(){
            return getUser().getBiggestPhoto();
        }

        public VKApiUserFull getUser() {
            return Memory.getUserById(userId);
        }

        public Object getExtra() {
            switch (updateType) {
                case Update.TYPE_ONLINE:
                    return 0 ;
                case Update.TYPE_OFFLINE:
                    return  (flags==0? 0:60*15) ;//таймаунт в секундах
                case Update.TYPE_USER_TYPING:
                    return "Пишет..";
                case Update.TYPE_CHAT_TYPING:
                    return "Пишет в беседе";
            }
            return null;
        }

        public boolean isStatusUpdate() {
            return updateType == TYPE_OFFLINE || updateType == TYPE_ONLINE;
        }
    }
}
