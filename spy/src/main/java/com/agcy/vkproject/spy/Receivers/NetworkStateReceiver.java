package com.agcy.vkproject.spy.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final ArrayList<NetworkStateChangeListener> listeners = new ArrayList<NetworkStateChangeListener>();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AGCY SPY","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()== NetworkInfo.State.CONNECTED) {
                Log.i("AGCY SPY","Network "+ni.getTypeName()+" connected");

                for(NetworkStateChangeListener listener : listeners){
                    listener.onConnected();
                }

            }
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            Log.d("AGCY SPY", "Connection lost");

        }
    }
    public static abstract class NetworkStateChangeListener {
        public NetworkStateChangeListener(){
            listeners.add(this);
        }
        public abstract void onConnected();
    }
}
