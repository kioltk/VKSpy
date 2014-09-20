package com.happysanta.vkspy.Longpoll;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Core.Memory;
import com.happysanta.vkspy.Core.UberFunktion;
import com.happysanta.vkspy.Helper.Time;
import com.happysanta.vkspy.R;
import com.happysanta.vkspy.Core.VKSdk;
import com.happysanta.vkspy.Receivers.NetworkStateReceiver;
import com.bugsense.trace.BugSenseHandler;
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
    public static final int ACTION_LOGOUT =-2;


    private String key;
    private String ts;
    private String server;

    private LongPollConnection connection;
    private static NetworkStateReceiver.NetworkStateChangeListener networkStateChangeListener;
    private int lastUpdate = 0;
    private static LongPollService instance;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        SharedPreferences preferences = getBaseContext().getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        lastUpdate = preferences.getInt("lastUpdate",0);
        if(networkStateChangeListener==null) {

            networkStateChangeListener = new NetworkStateReceiver.NetworkStateChangeListener(LONGPOLL_CONNECTION_ID) {
                @Override
                public void onConnected() {
                    restoreStatusesAndStartLongpoll();
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
                        Log.i("AGCY SPY LONGPOLLSERVICE", "force start");
                        //restoreSettings();
                        if (lastUpdate == 0)
                            updateStatusesAndStartLongpoll(Helper.FETCHING_FIRST);
                        else {
                            restoreSettings();
                            if(connection==null || (!connection.isCancelled() && connection.isFinished()))
                                restoreStatusesAndStartLongpoll();
                            else
                                startLongpollWithExternal();
                        }
                        break;
                    case ACTION_START_SAFE:

                        Log.i("AGCY SPY LONGPOLLSERVICE", "safe start ");
                        startSafe();
                        break;
                    case ACTION_STOP:
                        if (connection != null) {

                            Log.i("AGCY SPY LONGPOLLSERVICE", "stop ");
                            connection.cancel(true);
                            //connection = null;
                            saveSettings();
                            //stopSelf();
                            return Service.START_NOT_STICKY;
                        }
                        break;
                    case ACTION_LOGOUT:
                        if (connection != null) {
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



    private void startSafe() {

        Helper.initialize(getApplicationContext());

        VKSdk.initialize(getApplicationContext());

        Memory.loadUsers();

        restoreStatusesAndStartLongpoll();

        Log.i("AGCY SPY LONGPOLLSERVICE", "Started safe" + " key: " + key);
    }

    @Override
    public void onCreate() {

        // berore start
        // only on first startService and restart

        instance = this;


        super.onCreate();
        Log.i("AGCY SPY LONGPOLLSERVICE", "created"+" key: " + key );

    }
    @Override
    public void onDestroy() {
        Log.w("AGCY SPY LONGPOLLSERVICE", "destroyed" + " key: " + key);
        if(connection!=null)
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

                        startLongpollWithExternal();

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

    private void startLongpollWithExternal() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String intervalString = prefs.getString("connection_external_interval", "30");
        if(intervalString.equals("false"))
            intervalString = "30";
        int interval = Integer.parseInt(intervalString);
        int lastExternalUpdate = prefs.getInt("connection_external_lastupdate", 0);

        if (Time.getUnixNow() - lastExternalUpdate < interval) {
            startLongpoll();
            return;
        }
        int timeout = Time.getUnixNow() - lastExternalUpdate > 500 ? Helper.FETCHING_HOUR : Helper.FETCHING_EXTERNAL;

        ArrayList<VKApiUserFull> externals = Memory.getExternals();
        if (!externals.isEmpty()) {
            Helper.updateExternals(externals,timeout,new Runnable() {
                @Override
                public void run() {

                    startLongpoll();
                    prefs.edit().putInt("connection_external_lastupdate", Time.getUnixNow()).commit();
                }
            });
        } else {
            startLongpoll();
        }
    }
    private void restoreStatusesAndStartLongpoll() {
        if(Time.getUnixNow() - lastUpdate>60*60)
            updateStatusesAndStartLongpoll(Helper.FETCHING_HOUR);
        else
            updateStatusesAndStartLongpoll(Helper.FETCHING_FAST);
    }

    private void updateStatusesAndStartLongpoll(final int timeout) {
        Helper.updateStatuses(timeout, new Runnable(){
            @Override
            public void run() {
                saveLongpollExecuted();
                refreshSettings();
            }
        });
    }

    private void startLongpoll(){
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

                if(lastUpdate + 300 < Time.getUnixNow()) {
                    Log.e("AGCY SPY LONGPOLL","Last update was too long ago");
                    BugSenseHandler.sendEvent("Last update was too long ago");
                    ArrayList<Update> updates = new ArrayList<Update>();
                    for (int i = 0; i < updatesJson.length(); i++) {
                        try {
                            JSONArray updateJsonArray = new JSONArray(updatesJson.getString(i));

                            if (isTyping(updateJsonArray)) {
                                updates.add(new Update(updateJsonArray));
                            }

                        } catch (Exception exp) {
                            Log.e("AGCY SPY LONGPOLL", "update parsing error: " + exp.toString());
                        }
                    }
                    if (!updates.isEmpty())
                        Helper.newUpdates(updates);
                    restoreStatusesAndStartLongpoll();

                    LongPollService.this.ts = ts;
                    saveSettings();
                    saveLongpollExecuted();
                }else {
                    LongPollService.this.ts = ts;
                    saveSettings();
                    ArrayList<Update> updates = new ArrayList<Update>();
                    for (int i = 0; i < updatesJson.length(); i++) {
                        try {
                            JSONArray updateJsonArray = new JSONArray(updatesJson.getString(i));

                            if (shouldHandle(updateJsonArray))
                                updates.add(new Update(updateJsonArray));


                        } catch (Exception exp) {
                            Log.e("AGCY SPY LONGPOLL", "update parsing error: " + exp.toString());
                        }
                    }

                    Log.i("AGCY SPY", "longoll updates count: " + updates.size());
                    if (!updates.isEmpty())
                        Helper.newUpdates(updates);
                    SharedPreferences durovPrefs = getBaseContext().getSharedPreferences("durov", MODE_MULTI_PROCESS);
                    if(Memory.users.getById(1)!=null) {
                        int lastDurovUpdate = durovPrefs.getInt("lastUpdate", 0);
                        if (Time.getUnixNow()-lastDurovUpdate>10*60){
                            UberFunktion.initializeBackground(getBaseContext());
                        }
                    }
                    startLongpollWithExternal();
                    saveLongpollExecuted();
                }
            }

            @Override
            public void onError(Exception exp) {
                    Log.e("AGCY SPY LONGPOLL","", exp);
                    //BugSenseHandler.sendExceptionMessage("Longpoll","Execution error",exp);
                if(exp instanceof SocketException){
                    // ничего не делаем, при переподключении вай-фай всё востановится через NetworkStateChangeListener
                }
                if(exp instanceof UnknownHostException){
                    refreshSettings(false);
                }
                if(exp instanceof org.apache.http.conn.HttpHostConnectException){
                    startLongpollWithExternal();
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
        lastUpdate = Time.getUnixNow();
        SharedPreferences preferences = getBaseContext().getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastUpdate",lastUpdate);
        editor.commit();

    }
    public static Boolean isTyping(JSONArray updateArray){

        int updateType = 0;
        try {
            updateType = Integer.valueOf(updateArray.getInt(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return updateType == Update.TYPE_USER_TYPING;
    }
    public static Boolean shouldHandle(JSONArray updateArray) {

        int updateType =0;
        try {
            updateType = Integer.valueOf(updateArray.getInt(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (updateType) {
            case Update.TYPE_ONLINE:
            case Update.TYPE_OFFLINE:
            case Update.TYPE_USER_TYPING:
            case Update.TYPE_MESSAGE:
            case Update.TYPE_CHAT_TYPING:
                return true;
            default:
                return false;
        }
    }

    public static Update getUpdate(int type, int flags, int userid) {
        return instance.createUpdate(type,flags,userid);
    }
    public Update createUpdate(int type, int flags, int userid){
        return new Update(type,flags,userid);
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
        //private final String title;
        public Update(int type,int flags,int userid){
            this.updateType = type;
            this.flags = flags;
            this.userId = userid;
        }

        public Update(JSONArray updateJsonArray) {
            int tempUserId = 0;
            int tempFlags = 0;
            String tempTitle = "...";
            int tempUpdateType = 0;
            try {
                tempUpdateType = (updateJsonArray.getInt(0));
                switch (tempUpdateType) {

                    // сообщение в любом случае приходит от юзера.
                    // если getExtra() = 0, тогда это обычное сообщение.
                    // иначе getExtra() = chatId

                    case TYPE_MESSAGE:
                        tempFlags = (updateJsonArray.getInt(2));
                        if ((tempFlags & FLAG_MESSAGE_CHAT) == FLAG_MESSAGE_CHAT) {
                            tempFlags = (updateJsonArray.getInt(3)) - 2000000000;

                            try {
                                JSONObject attaches = new JSONObject(updateJsonArray.getString(7));
                                tempUserId = attaches.getInt("from");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            tempFlags = 0;
                            tempUserId = (updateJsonArray.getInt(3));
                        }
                        break;
                    case TYPE_CHAT_TYPING:
                        tempFlags = (updateJsonArray.getInt(2));
                    case TYPE_USER_TYPING:
                        tempUserId = (updateJsonArray.getInt(1));
                        break;
                    // online или offline
                    default:
                        tempUserId = updateJsonArray.getInt(1) * -1;
                        tempFlags = (updateJsonArray.getInt(2));
                        break;
                }
            }catch (Exception exp){
                exp.printStackTrace();
            }
            flags = tempFlags;
            userId = tempUserId;
            //title = tempTitle;
            updateType = tempUpdateType;
        }

        public int getType(){return updateType;}
        public String getHeader(){
            VKApiUserFull user = getUser();
            return user.first_name+" "+user.last_name;
        }
        public String getMessage(){


            VKApiUserFull user = getUser();
            switch (updateType) {
                case Update.TYPE_MESSAGE:
                    return "message";
                case Update.TYPE_ONLINE:
                    return (user.sex ==2?getString(R.string.come_online_m):getString(R.string.come_online_f) ) ;
                case Update.TYPE_OFFLINE:
                    return (user.sex ==2?getString(R.string.gone_offline_m):getString(R.string.gone_offline_f) ) ;
                case Update.TYPE_USER_TYPING:
                    return (user.sex ==2?getString(R.string.was_writing_m):getString(R.string.was_writing_f) ) ;
                case Update.TYPE_CHAT_TYPING:
                    return (user.sex ==2?getString(R.string.was_writing_chat_m):getString(R.string.was_writing_chat_f) ) ;
            }
            return "Уведомление";
        }
        public String getImageUrl(){
            return getUser().getBiggestPhoto();
        }

        public VKApiUserFull getUser() {
            return Memory.getUserById(userId);
        }
        public int getUserId(){
            return userId;
        }
        public Object getExtra() {
            switch (updateType) {
                case Update.TYPE_ONLINE:
                    return flags;
                case Update.TYPE_OFFLINE:
                    return (flags == 0 ? 0 : 60 * 15);//таймаунт в секундах
                case Update.TYPE_USER_TYPING:
                    return 0;
                case Update.TYPE_CHAT_TYPING:
                    return flags;
                case TYPE_MESSAGE:
                    if (flags != 0)
                        return flags;
                    return 0;
            }
            return null;
        }

        public boolean isStatusUpdate() {
            return updateType == TYPE_OFFLINE || updateType == TYPE_ONLINE;
        }


        public boolean isChatMessage() {

            return updateType == TYPE_MESSAGE && flags != 0;
        }
    }
}
