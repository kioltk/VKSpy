package com.agcy.vkproject.spy;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.agcy.vkproject.spy.Core.LongPollConnection;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Core.Notificator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.api.model.VKApiUserFull;

import org.json.JSONArray;

import java.util.ArrayList;

public class LongPollService extends Service {

    private String key;
    private String ts;
    private String server;

    private LongPollConnection connection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("AGCY SPY","LongpollService " + (intent == null? "restarted after crash":"started") );

        if(intent==null){

            Memory.initialize(getApplicationContext());
            Notificator.initialize(getApplicationContext());

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
            ImageLoader.getInstance().init(config);
        }

        restoreConnection();
        createConnection();
        return Service.START_STICKY;

    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.i("AGCY SPY", "Longpollservice created");

    }
    @Override
    public void onDestroy() {
        Log.w("AGCY SPY LONGPOLL","Longpollservice destroys");
        connection.cancel(true);
        saveConnection();
    }

    private void saveConnection() {

        SharedPreferences.Editor preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS).edit();
        preferences.putString("server", server);
        preferences.putString("ts", ts);
        preferences.putString("key", key);
        preferences.commit();

    }
    private void restoreConnection(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll",MODE_MULTI_PROCESS);
        server = preferences.getString("server","");
        ts = preferences.getString("ts","");
        key = preferences.getString("key","");

    }
    private void createConnection() {

        connection = new LongPollConnection(server,key,ts) {
            @Override
            public void onSuccess(JSONArray updatesJson, String ts) {
                LongPollService.this.ts = ts;
                saveConnection();
                ArrayList<Update> updates = new ArrayList<Update>();
                for(int i = 0;i< updatesJson.length(); i++){
                    try {
                        String[] updateJson = updatesJson.getString(i)
                                .replace("[", "")
                                .replace("]", "")
                                .split(",");
                        if(shouldHandle(updateJson))
                            updates.add(new Update(updateJson));

                    }
                    catch (Exception exp){
                        Log.e("AGCY SPY", "update parsing error: "+exp.toString());
                    }
                }
                Memory.saveUpdates(updates);
                Notificator.announce(updates);
                createConnection();
            }

            @Override
            public void onError(Exception exp) {

                Log.e("AGCY SPY LONGPOLL", exp.toString() + "" + exp.getMessage());
                createConnection();
            }
        };
        connection.execute();
        Log.i("AGCY SPY","Longpoll executed");
    }

    public static Boolean shouldHandle(String[] updateArray) {

        int updateType = Integer.valueOf(updateArray[0]);
        switch (updateType) {
            case Update.TYPE_ONLINE:
            case Update.TYPE_OFFLINE:
            case Update.TYPE_USER_TYPING:
            case Update.TYPE_CHAT_TYPING:
                return true;
            default:
                return false;
        }
    }
    
    public class Update {

        public static final int TYPE_ONLINE = 8;
        public static final int TYPE_OFFLINE = 9;
        public static final int TYPE_USER_TYPING = 61;
        public static final int TYPE_CHAT_TYPING = 62;

        private final int updateType;
        private final int flags;
        private final int userId;
        private VKApiUserFull user;
        
        public Update(String[] updateArray){
            this.updateType = Integer.valueOf(updateArray[0]);
            this.userId = Integer.valueOf(updateArray[1]) * (updateType<10? -1: 1);
            this.flags = Integer.valueOf(updateArray[2]);
            user = Memory.getUserById(userId);
        }

        public int getType(){return updateType;}
        public String getHeader(){
            return user.first_name+" "+user.last_name;
        }
        public String getMessage(){
            switch (updateType) {
                case Update.TYPE_ONLINE:
                    return (user.sex ==2?"Зашёл в сеть":"Зашла в сеть" ) ;
                case Update.TYPE_OFFLINE:
                    return (user.sex ==2?"Вышел из сети":"Вышла из сети" ) ;
                case Update.TYPE_USER_TYPING:
                    return "Пишет..";
                case Update.TYPE_CHAT_TYPING:
                    return "Пишет в беседе";
            }
            return "Уведомление";
        }
        public String getImageUrl(){
            return user.photo_100;
        }

        public VKApiUserFull getUser() {
            return user;
        }
    }
}
