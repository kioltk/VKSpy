package com.happysanta.crazytyping.Longpoll;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.happysanta.crazytyping.Core.Helper;
import com.happysanta.crazytyping.Core.Memory;
import com.happysanta.crazytyping.Helper.Time;
import com.happysanta.crazytyping.R;
import com.happysanta.crazytyping.Core.VKSdk;
import com.happysanta.crazytyping.Receivers.NetworkStateReceiver;
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

public class CrazyTypingService extends Service {

    private static final String CRAZYTYPER_TAG = "CRAZYTYPER SERVICE";
    private static boolean enabled;


    private static CrazyTyper connection;
    private static NetworkStateReceiver.NetworkStateChangeListener networkStateChangeListener;
    private static CrazyTypingService instance;

    public static boolean isTyping() {
        return connection != null;
    }

    public static void setEnabled(boolean enabled) {
        CrazyTypingService.enabled = enabled;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if(intent==null){
            Log.i(CRAZYTYPER_TAG, "Intent equals null" );
            startSafe();
        }else{
            startTyper();
        }
        return START_STICKY;
    }

    private void startTyper() {

        if(connection!=null)
            connection.cancel();

        connection = new CrazyTyper();
        connection.start();

    }


    private void startSafe() {

        Helper.initialize(getApplicationContext());

        VKSdk.initialize(getApplicationContext());
        Memory.loadUsers();
        Memory.loadDialogs();

        startTyper();

        Log.i(CRAZYTYPER_TAG, "Started safe");
    }

    @Override
    public void onCreate() {
        instance = this;

        super.onCreate();
        Log.i(CRAZYTYPER_TAG, "created");

    }
    @Override
    public void onDestroy() {
        Log.w(CRAZYTYPER_TAG, "destroyed");
        if(connection!=null)
            connection.cancel();
    }



    private Boolean checkLongpollEnabled(){

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("longpoll", MODE_MULTI_PROCESS);
        return preferences.getBoolean("status",true);
    }


    public static Boolean toggle() {

        enabled = !enabled;

        if(!enabled) {
            stop();
        }else{
            connection = new CrazyTyper();
            connection.start();
        }
        return enabled;
    }

    public static void stop() {
        if(connection!=null){
            connection.cancel();
            connection = null;
        }
    }
}
