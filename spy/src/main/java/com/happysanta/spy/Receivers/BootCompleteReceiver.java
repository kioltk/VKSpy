package com.happysanta.spy.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.happysanta.spy.Longpoll.LongPollService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("start", Context.MODE_MULTI_PROCESS);
        Boolean enabled = preferences.getBoolean("startOnBoot", true);
        if(enabled) {
            Log.i("AGCY SPY","Starting on boot");
            Intent longPollService = new Intent(context, LongPollService.class);
            Bundle bundle = new Bundle();
            bundle.putInt(LongPollService.ACTION,LongPollService.ACTION_START_SAFE);
            longPollService.putExtras(bundle);
            context.startService(longPollService);

        }
    }
}
