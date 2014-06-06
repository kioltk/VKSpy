package com.agcy.vkproject.spy.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.agcy.vkproject.spy.Core.Memory;

import java.util.HashMap;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final HashMap<Integer,NetworkStateChangeListener> listeners = new HashMap<Integer,NetworkStateChangeListener>();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AGCY SPY","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()== NetworkInfo.State.CONNECTED) {
                Log.i("AGCY SPY","Network "+ni.getTypeName()+" connected");

                for(NetworkStateChangeListener listener : listeners.values()){
                    listener.onConnected();
                    Memory.saveNetwork(true);
                }
                //listeners.clear();
            }
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {

            for(NetworkStateChangeListener listener : listeners.values()){
                listener.onLost();
                Memory.saveNetwork(false);
            }
            Log.d("AGCY SPY", "Connection lost");

        }
    }
    public static abstract class NetworkStateChangeListener {
        private final int id;
        public static final int DUROV_LOADER_NETWORK_LISTENER = 1;

        public NetworkStateChangeListener(int id){
            this.id = id;
            if (!listeners.containsKey(id))
                listeners.put(id,this);
        }
        public abstract void onConnected();
        public abstract void onLost();
        public void remove(){
            listeners.remove(id);
        }
    }
}
