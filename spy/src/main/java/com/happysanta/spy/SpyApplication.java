package com.happysanta.spy;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.Core.Logs;
import com.happysanta.spy.Core.Notificator;
import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.seppius.i18n.plurals.PluralResources;

/**
 * Created by kiolt_000 on 02-May-14.
 */
public class SpyApplication extends Application implements ExceptionCallback {

    @Override
    public void onCreate() {

        super.onCreate();
        Logs.removeLogs();
        //Logs.saveNewFile();
        try {
            Helper.pluralResources = new PluralResources( getResources() );
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BugSenseHandler.initAndStartSession(this,"694ab3f3");
        BugSenseHandler.setExceptionCallback(this);
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
        Notificator.clearNotifications();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i("AGCY SPY APPLICATION", "Low memory");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i("AGCY SPY APPLICATION","Terminate");
        BugSenseHandler.closeSession(this);
        Notificator.clearNotifications();

    }

    @Override
    public void lastBreath(Exception ex) {
    }
}
