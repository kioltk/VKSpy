package com.happysanta.crazytyping.Core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.happysanta.crazytyping.Helper.Type;
import com.happysanta.crazytyping.Longpoll.CrazyTypingService;
import com.happysanta.crazytyping.MainActivity;
import com.happysanta.crazytyping.Models.Online;
import com.happysanta.crazytyping.Models.Status;
import com.happysanta.crazytyping.Models.Update;
import com.happysanta.crazytyping.R;
import com.happysanta.crazytyping.Receivers.NetworkStateReceiver;
import com.bugsense.trace.BugSenseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.seppius.i18n.plurals.PluralResources;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiMessages;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Helper {

    public static final int FETCHING_FIRST = -1;
    public static final int FETCHING_FAST = 5 * 60;
    public static final int FETCHING_HOUR = 60 * 60;
    public static final int FETCHING_EXTERNAL = 9999;

    public static final int ONLINE = -1;
    public static final int NOW = -2;
    public static final int UNDEFINED = -3;

    private static final int CHAT_DOWNLOADER_ID = 152;
    public static final int START_LOADER_ID = 150;
    public static final int USER_DOWNLOADER_ID = 151;
    private static final int EXTERNAL_UPDATER_ID = 161;
    private static final int STATUSES_UPDATER_ID = 162;
    private static Context context;
    private static MainActivity mainActivity;
    public static PluralResources pluralResources;

    public static void initialize(Context context) {
        Helper.context = context;

        Memory.initialize(context);
        //Notificator.initialize(context);

        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }

        DisplayImageOptions displayimageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true).build();


        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).
                defaultDisplayImageOptions(displayimageOptions).build();
        ImageLoader.getInstance().init(config);

        VKSdk.initialize(context);
        BugSenseHandler.initAndStartSession(context,"07310e3f");
        Log.i("AGCY SPY","Initialization ended");
    }

    public static void logout() {

        Log.i("AGCY SPY", "Logout processing..");
        stopMainActivity();
        VKSdk.logout();
    }

    public static void DESTROYALL() {

        SharedPreferences preferences = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        clearAllPreferences();

        clearStartPreferences();

        //stopLongpoll();
        Memory.clearAll();
        //Notificator.DESTROY();
        clearApplicationData();

        context = null;

    }

    public static void clearStartPreferences() {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences("start", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void clearAllPreferences() {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        editor.clear();
        editor.commit();
        preferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        editor.clear();
        editor.commit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void clearApplicationData() {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }


    //region Updates
    /*
    public static void newUpdates(ArrayList<CrazyTypingService.Update> updates) {

        ArrayList<CrazyTypingService.Update> onlines = new ArrayList<CrazyTypingService.Update>();
        ArrayList<CrazyTypingService.Update> messages = new ArrayList<CrazyTypingService.Update>();
        //Log.i("AGSY SPY", "New updates");
        //onlines we can save immediately
        //String logString = "";
        for (CrazyTypingService.Update update : updates) {

            if (update.isStatusUpdate()) {
                onlines.add(update);
                //logString += "userid: " + update.getUser().id +" update type: "+ update.getType();
            }
        }
        Log.i("AGСY SPY", "Online updates: "+onlines.size());
        saveOnlines(onlines);
        Notificator.announce(onlines);
        // however other updates we have to check
        //Log.i("AGSY SPY", "Onlines saved");
        for (CrazyTypingService.Update update : updates) {
            if (update.getType() == CrazyTypingService.Update.TYPE_USER_TYPING) {
                Type.newTyping(update);
                continue;
            }
            if (update.getType() == CrazyTypingService.Update.TYPE_CHAT_TYPING) {
                Type.newChatTyping(update);
            }
        }

        for (CrazyTypingService.Update update : updates) {
            if (update.getType() == CrazyTypingService.Update.TYPE_MESSAGE && !update.isChatMessage()) {
                Log.i("AGСY SPY", "Message update from userid: " + update.getUserId());
                Type.newMessage(update);
                continue;
            }
            if (update.isChatMessage()) {
                Log.i("AGСY SPY", "Chat message update from userid: " + update.getUserId() + " in chatid: " + update.getExtra());
                Type.newChatMessage(update);
            }
        }

        Log.i("AGСY SPY", "Updates was processed");

    }

    public static void saveOnlines(ArrayList<CrazyTypingService.Update> updates) {
        for (CrazyTypingService.Update update : updates) {
            boolean isOnline = (update.getType() == CrazyTypingService.Update.TYPE_ONLINE);
                Memory.setStatus(
                        update.getUser(),
                        isOnline,
                        isOnline? false: ((Integer) update.getExtra()) > 0,
                        isOnline? (Integer) update.getExtra() :null);


        }
    }
    */

    public static void downloadUser(final int userid) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.USER_IDS, userid);
        parameters.put("fields", "photo_200,photo_200_orig,photo_50,photo_100");

        VKRequest donwloadUserRequest = VKApi.users().get(parameters);
        donwloadUserRequest.executeWithListener(
                new VKRequest.VKRequestListener() {
                    public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                    @Override
                    public void onComplete(final VKResponse response) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Memory.saveUser(((VKList<VKApiUserFull>) response.parsedModel).get(0));
                                Memory.downloadingUsersIds.remove((Integer) userid);

                            }
                        }).start();
                        if (connectionListener != null)
                            connectionListener.remove();
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        //unknownhost

                        if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {

                            connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(USER_DOWNLOADER_ID) {
                                @Override
                                public void onConnected() {

                                    downloadUser(userid);

                                }

                                @Override
                                public void onLost() {
                                }

                            };
                        }
                        if (error.httpError != null)
                            Log.e("AGCY SPY", error.httpError.toString());
                        else

                            Log.e("AGCY SPY", error.toString());
                    }
                }
        );
    }
    //endregion
    //region Helpers
    private static Float dpValue = null;
    public static int convertToDp(int value) {
        if(dpValue==null) {
            Resources r = context.getResources();
            DisplayMetrics metrics = r.getDisplayMetrics();
            dpValue =  (metrics.density);
        }
        int px = (int) (value * dpValue);
        return px;
    }


    public static void setMainActivity(MainActivity mainActivity) {
        Helper.mainActivity = mainActivity;
    }

    public static void stopMainActivity() {
        Log.i("AGCY SPY", "Main activity stopped");
        if (mainActivity != null) {
            mainActivity.finish();
            mainActivity = null;
        }
    }

    public static boolean isInitialized() {
        return context != null;
    }

    public static String getUberFunctionUrl() {
        return "http://happysanta.org/durov.php?count=%d&offset=%d";
    }
    static boolean fetching = false;

    public static String getQuantityString(int resId, int quantityIndicator, int quantityDecimal) {
        return pluralResources.getQuantityString(resId,quantityIndicator,quantityDecimal);
    }

    public static boolean isFetching() {
        return fetching;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static String getPlatform(int platform) {
        switch (platform) {
            case 1:
                return context.getString(R.string.mobile);
            case 2:
                return context.getString(R.string.iphone);
            case 3:
                return context.getString(R.string.ipad);
            case 4:
                return context.getString(R.string.android);
            case 5:
                return context.getString(R.string.wphone);
            case 6:
                return context.getString(R.string.windowse);
            default:
                return null;

        }
    }

    public static Context getContext() {
        return context;
    }

    //endregion
    public static ArrayList<? extends Update> convertToStatus(ArrayList<Online> onlines) {

        ArrayList<Status> convertedOnlines = new ArrayList<Status>();

        for (Online online : onlines) {
            int userid = online.getOwner().id;
            int till = online.getTill();
            int since = online.getSince();
            if (till > 0)
                convertedOnlines.add(new Status(userid, till, false));
            if (since > 0)
                convertedOnlines.add(new Status(userid, since, true, online.getPlatform()));

        }

        Collections.sort(convertedOnlines, new CustomComparator());
        Collections.reverse(convertedOnlines);
        return convertedOnlines;
    }

    public static ArrayList<Online> convertLastUndefinedToOnline(ArrayList<Online> onlines) {
        //
        //if(!asc)
        //Collections.reverse(onlines);
        if(onlines.isEmpty()){
            return onlines;
        }

        Online online = onlines.get(0);
        if (online.getTill() == 0 && online.getOwner().online) {
            online.setTill(ONLINE);
        }


        return onlines;
    }

    public static class CustomComparator implements Comparator<Update> {
        @Override
        public int compare(Update o1, Update o2) {
            return o1.getUnix().compareTo(o2.getUnix());
        }
    }

    // listeners сюда вообще не смотреть
    public static void trackedUpdated() {
        if(trackUpdatedListener!=null)
            trackUpdatedListener.onUpdate();
    }
    public static void setTrackUpdatedListener(TrackUpdatedListener trackUpdatedListener){
        Helper.trackUpdatedListener = trackUpdatedListener;
    }
    public static abstract class TrackUpdatedListener{
        public abstract void onUpdate();
    }
    private static TrackUpdatedListener trackUpdatedListener;

    private static ArrayList<InitializationListener> initializationEndListeners = new ArrayList<InitializationListener>();

    public static void addInitializationListener(InitializationListener listener) {
        initializationEndListeners.add(listener);
    }


    private static boolean loadingEnded = false;
    private static boolean downloadingEnded = false;

    public static void loadingEnded() {

        loadingEnded = true;
        for (InitializationListener listener : initializationEndListeners) {
            listener.onLoadingEnded();
        }
    }

    public static void downloadingEnded() {
        downloadingEnded = true;
        for (InitializationListener listener : initializationEndListeners) {
            listener.onDownloadingEnded();
        }

    }

    public static boolean isLoaded() {
        return loadingEnded;
    }

    public static boolean isDownloaded() {
        return downloadingEnded;
    }

    public static abstract class InitializationListener {
        public abstract void onLoadingEnded();

        public abstract void onDownloadingEnded();
    }

}