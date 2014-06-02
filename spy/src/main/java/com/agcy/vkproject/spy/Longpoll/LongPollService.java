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
import com.agcy.vkproject.spy.Core.UberFunktion;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.bugsense.trace.BugSenseHandler;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

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
    public static final int ACTION_LOGOUT =-2;


    private String key;
    private String ts;
    private String server;

    private LongPollConnection connection;
    private static NetworkStateReceiver.NetworkStateChangeListener networkStateChangeListener;
    private int lastUpdate = 0; // 0 если первый запуск


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences preferences = getBaseContext().getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        lastUpdate = preferences.getInt("lastUpdate",0);
        if(networkStateChangeListener==null) {

            networkStateChangeListener = new NetworkStateReceiver.NetworkStateChangeListener(LONGPOLL_CONNECTION_ID) {
                @Override
                public void onConnected() {
                    updateStatusesAndStartLongpoll(0);
                }

                @Override
                public void onLost() {
                    if(connection!=null)
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
                        //restoreSettings();
                        if (lastUpdate==0)
                            updateStatusesAndStartLongpoll(Helper.FIRST_LOADING);
                        else {
                            restoreSettings();
                            updateStatusesAndStartLongpoll(0);
                        }
                        break;
                    case ACTION_START_SAFE:

                        Log.i("AGCY SPY LONGPOLLSERVICE","safe start " );
                        startSafe();
                        break;
                    case ACTION_STOP:
                        if (connection != null) {

                            Log.i("AGCY SPY LONGPOLLSERVICE","stop " );
                            connection.cancel(true);
                            connection = null;
                            saveSettings();
                            //stopSelf();
                            return Service.START_NOT_STICKY;
                        }
                        break;
                    case ACTION_LOGOUT:
                        if(connection!=null){
                            connection.cancel(true);
                            connection = null;
                            clearSettings();
                            return START_NOT_STICKY;
                        }

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

    private void updateStatusesAndStartLongpoll(final int timeout) {
        Helper.updateStatuses(timeout,new Runnable(){
            @Override
            public void run() {
                saveLongpollExecuted();
                startLongpoll();
            }
        });
    }

    private void startSafe() {

        Helper.initialize(getApplicationContext());

        com.agcy.vkproject.spy.Core.VKSdk.initialize(getApplicationContext());

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
        Log.w("AGCY SPY LONGPOLLSERVICE", "destroyed" + " key: " + key);
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
    private void refreshSettings(final boolean saveNewTs) {



        Log.w("AGCY SPY LONGPOLLSERVICE","refreshing..");
        try{
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
                    Log.w("AGCY SPY LONGPOLLSERVICE", "refreshing error" + error);
                }

                @Override
                public void onComplete(VKResponse response) {

                    try {

                        JSONObject responseJson = response.json.getJSONObject("response");
                        if(saveNewTs)
                            ts = responseJson.getString("ts");
                        server = responseJson.getString("server");
                        key = responseJson.getString("key");

                        Log.w("AGCY SPY LONGPOLLSERVICE", "refreshing complete");

                        saveSettings();

                        startLongpoll();

                    } catch (Exception exp) {
                        Log.e("AGCY SPY LONGPOLL", "refreshing error", exp);
                        BugSenseHandler.sendExceptionMessage("Longpoll","Refreshing error",exp);
                    }
                }
            });
        }catch (Exception exp){

            Log.e("AGCY SPY LONGPOLLSERVICE","refreshing error reinit ",exp);

            BugSenseHandler.sendExceptionMessage("Longpoll","Refreshing error. Need reinit",exp);
            Helper.initialize(getApplicationContext());
            refreshSettings();
            return;
        }
        Log.w("AGCY SPY LONGPOLLSERVICE", "refreshing executed ");
    }
    private void restoreSettings(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        server = preferences.getString("server","");
        ts = preferences.getString("ts","");
        key = preferences.getString("key","");

    }
    private void clearSettings(){

        server = null;
        ts = null;
        key = null;

        SharedPreferences.Editor preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS).edit();
        preferences.putString("server", server);
        preferences.putString("ts", ts);
        preferences.putString("key", key);
        preferences.commit();
        preferences.clear();

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

                if(lastUpdate + 300 < Helper.getUnixNow()) {
                    Log.e("AGCY SPY LONGPOLL","Last update was too long ago");
                    BugSenseHandler.sendEvent("Last update was too long ago");
                    ArrayList<Update> updates = new ArrayList<Update>();
                    for (int i = 0; i < updatesJson.length(); i++) {
                        try {
                            String[] updateJson = updatesJson.getString(i)
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(",");
                            if (isTyping(updateJson))
                                updates.add(new Update(updateJson));


                        } catch (Exception exp) {
                            Log.e("AGCY SPY LONGPOLL", "update parsing error: " + exp.toString());
                        }
                    }
                    if (!updates.isEmpty())
                        Helper.newUpdates(updates);
                    updateStatusesAndStartLongpoll(lastUpdate>60*60?Helper.TIMEOUT_1HOUR:Helper.TIMEOUT_5MINS);

                    LongPollService.this.ts = ts;
                    saveSettings();
                    saveLongpollExecuted();
                }else {
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

                    Log.i("AGCY SPY", "longoll updates count: " + updates.size());
                    if (!updates.isEmpty())
                        Helper.newUpdates(updates);
                    SharedPreferences durovPrefs = getBaseContext().getSharedPreferences("durov", MODE_MULTI_PROCESS);
                    if(Memory.users.getById(1)!=null) {
                        durovPrefs.getInt("lastUpdate", 0);
                        if (Helper.getUnixNow()-lastUpdate>10*60){
                            UberFunktion.initializeBackground(getBaseContext());
                        }
                    }
                    startLongpoll();
                    saveLongpollExecuted();
                }
            }

            @Override
            public void onError(Exception exp) {
                    Log.e("AGCY SPY LONGPOLL","", exp);
                    //BugSenseHandler.sendExceptionMessage("Longpoll","Execution error",exp);
                if(exp instanceof UnknownHostException){
                    refreshSettings(false);
                }
                if(exp instanceof org.apache.http.conn.HttpHostConnectException){
                    startLongpoll();
                }
                if(exp instanceof ServerException){
                    BugSenseHandler.sendEvent("Server settings error");
                    Log.e("AGCY SPY LONGPOLL","Server settings error",exp);
                    refreshSettings(true);
                }
                if (exp instanceof JSONException) {
                    Log.e("AGCY SPY LONGPOLL","Response error",exp);
                    BugSenseHandler.sendExceptionMessage("Longpoll","Response error",exp);
                    refreshSettings(false);
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

    private void refreshSettings() {
        refreshSettings(true);
    }

    private Boolean checkLongpollEnabled(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        return preferences.getBoolean("status",true);
    }
    private void saveLongpollExecuted(){
        lastUpdate = Helper.getUnixNow();
        SharedPreferences preferences = getBaseContext().getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastUpdate",lastUpdate);
        editor.commit();

    }
    public static Boolean isTyping(String[] updateArray){

        int updateType = Integer.valueOf(updateArray[0]);
        return updateType == Update.TYPE_USER_TYPING;
    }
    public static Boolean shouldHandle(String[] updateArray) {

        int updateType = Integer.valueOf(updateArray[0]);
        switch (updateType) {
            case Update.TYPE_ONLINE:
            case Update.TYPE_OFFLINE:
            case Update.TYPE_USER_TYPING:
                return true;
            case Update.TYPE_MESSAGE:
                int flags = Integer.valueOf(updateArray[2]);
                return (flags & Update.FLAG_MESSAGE_CHAT) != Update.FLAG_MESSAGE_CHAT;
            case Update.TYPE_CHAT_TYPING:
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
        public static final int TYPE_CHAT_TYPING = 62;// rebuild architecture to get chats also

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
                    return (user.sex ==2?getString(R.string.come_online_m):getString(R.string.come_online_f) ) ;
                case Update.TYPE_OFFLINE:
                    return (user.sex ==2?getString(R.string.gone_offline_m):getString(R.string.gone_offline_f) ) ;
                case Update.TYPE_USER_TYPING:
                    return (user.sex ==2?getString(R.string.was_writing_m):getString(R.string.was_writing_f) ) ;
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

        public String getShortMessage() {
            return getUser().first_name;
        }
    }
}
