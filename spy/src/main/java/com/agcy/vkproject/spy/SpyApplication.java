package com.agcy.vkproject.spy;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.util.Log;

/**
 * Created by kiolt_000 on 02-May-14.
 */
public class SpyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("AGCY SPY APPLICATION"," HAHA CRASH!");
            }
        });
        */
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.i("AGCY SPY APPLICATION","Trim");

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i("AGCY SPY APPLICATION", "Terminate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i("AGCY SPY APPLICATION","Terminate");

    }

}
