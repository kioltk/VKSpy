package com.agcy.vkproject.spy.Core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.MainActivity;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
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
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;

public class Helper {

    public static final int START_LOADER_ID = 150;
    public static final int START_DOWNLOADER_ID = 151;
    public static final int FIRST_LOADING = -1;
    public static final int TIMEOUT_5MINS = 5 * 60;
    public static final int TIMEOUT_1HOUR = 60 * 60;
    public static final int ONLINE = -1;
    public static final int NOW = -2;
    public static final int UNDEFINED = -3;
    private static Context context;
    private static MainActivity mainActivity;
    public static PluralResources pluralResources;
    private static int timeFormat = 0;

    public static void initialize(Context context) {
        Helper.context = context;

        Memory.initialize(context);
        Notificator.initialize(context);

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
    }
    static Handler minuteTimerHandler;
    static TimerTask minuteTimer = new TimerTask() {
        @Override
        public void run() {
            try {
                Thread.sleep(60*1000);
                for (Runnable timerListener : timerListeners) {
                    minuteTimerHandler.post(timerListener);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    } ;
    static ArrayList<Runnable> timerListeners = new ArrayList<Runnable>();
    public static void addMinuteTimer(Runnable timerListener){
        if(minuteTimerHandler==null) {
            minuteTimerHandler = new Handler();
        }
        timerListeners.add(timerListener);
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

        stopLongpoll();
        Memory.clearAll();
        Notificator.DESTROY();
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
    }

    public static void stopLongpoll() {
        Intent stopLongpoll = new Intent(context, LongPollService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(LongPollService.ACTION, LongPollService.ACTION_STOP);
        stopLongpoll.putExtras(bundle);
        context.startService(stopLongpoll);
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
    public static void newUpdates(ArrayList<LongPollService.Update> updates) {

        ArrayList<LongPollService.Update> onlines = new ArrayList<LongPollService.Update>();
        ArrayList<LongPollService.Update> messages = new ArrayList<LongPollService.Update>();
        //Log.i("AGSY SPY", "New updates");
        //onlines we can save immediately
        for (LongPollService.Update update : updates) {

            if (update.isStatusUpdate()) {
                onlines.add(update);
            }
        }
        saveOnlines(onlines);
        Notificator.announce(onlines);
        // however other updates we have to check
        //Log.i("AGSY SPY", "Onlines saved");
        for (LongPollService.Update update : updates) {
            if (update.getType() > 10) {
                newTyping(update);
            }
        }

        for (LongPollService.Update update : updates) {
            if (update.getType() == LongPollService.Update.TYPE_MESSAGE) {
                newMessage(update);
            }
        }

        Log.i("AGСY SPY", "Updates was processed");

    }

    public static void saveOnlines(ArrayList<LongPollService.Update> updates) {
        for (LongPollService.Update update : updates) {
            Memory.setStatus(
                    update.getUser(),
                    (update.getType() == LongPollService.Update.TYPE_ONLINE),
                    ((Integer) update.getExtra()) > 0
            );
        }
    }


    private static void newMessage(LongPollService.Update message) {
        denyTyping(message.getUser());
    }

    static HashMap<Integer, TypingTimer> typingTimers = new HashMap<Integer, TypingTimer>();

    public static void downloadUser(final int userid) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.USER_IDS, userid);
        parameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100,online,last_seen");

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
                                Memory.downloadingIds.remove((Integer) userid);

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

                            connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(START_DOWNLOADER_ID) {
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

    public static int getUnixNow() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    public static String getDate(int unix) {
        Date date = new Date(unix * 1000L);

        return String.format("%1$td %1$tB",
                date);
        //return String.format("%2$td %2$tB", date);
        //return ft.format(date);
    }

    public static String getTime(int unix) {

        switch (unix){
            case NOW:
                return context.getString(R.string.now);
            case UNDEFINED:
                return context.getString(R.string.undefined);
        }

        Date date = new Date(unix * 1000L);

            if (Helper.getTimeFormat() == 24) {

                return String.format("%tT",
                        date);

            }
        int hrs = date.getHours();
        Boolean isPM = hrs > 12;
        return String.format("%1$tl:%1$tM:%1$tS " + (isPM ? "pm" : "am"),
                date);

    }

    public static String getSmartDate(int time) {
        Resources res = context.getResources();
        if (time == NOW)
            return res.getString(R.string.now);
        if(time==UNDEFINED){
            return res.getString(R.string.undefined);
        }
        if (isToday(time))
            return res.getString(R.string.today);
        if (getDate( getUnixNow() - 24 * 3600).equals(getDate(time)))
            return res.getString(R.string.yesterday);
        return getDate(time);
    }

    public static String getSmartTime(Integer unix) {
        Resources res = context.getResources();
        if (isToday(unix)) {
            long time = getUnixNow() - unix;
            if (time < 3600 * 12) {
                if (time < 3600) {
                    int minutes = (int) time / 60;
                    if (minutes == 0)
                        return res.getString(R.string.moment);
                    return minutes + " " + res.getString(R.string.min);
                }
                if (time > 3600 && time < 7200)
                    return 1 + " " + res.getString(R.string.hr);
                return time / 3600 + " " + res.getString(R.string.hrs);
            }
        }
        return getTimeShort(unix);
    }

    public static String getTimeShort(int unix) {

        Date date = new Date(unix * 1000L);

            if (getTimeFormat() == 24) {

                return String.format("%tR",
                        date);

            }
        int hrs = date.getHours();
        Boolean isPM = hrs > 12;
        return String.format("%1$tl:%1$tM " + (isPM ? "pm" : "am"),
                date);
    }

    public static boolean isOnline(int userid) {
        return Memory.getUserById(userid).online;
    }


    public static String getStreak(int till, int since) {

        Resources res = context.getResources();
        if (till <= 0 || since <= 0)
            return res.getString(R.string.undefined);
        String convertedStreak = "";
        int streak = till - since;
        if (streak < 5) {
            return res.getString(R.string.moment);
        }
        if (streak / (24 * 3600) > 0) {
            convertedStreak += streak / (24 * 3600) + " "+ res.getString(R.string.day)+" ";
            streak = streak % (24 * 3600);
        }
        if (streak / (3600) > 0) {
            int hrOrHrs = streak / 3600;
            if (hrOrHrs == 1)
                convertedStreak += 1 + " "+res.getString(R.string.hr)+" ";
            else
                convertedStreak += hrOrHrs +" "+res.getString(R.string.hrs)+" ";
            streak = streak % (3600);
        }
        if (streak / 60 > 0) {
            convertedStreak += streak / 60 + " "+res.getString(R.string.min)+" ";
            streak = streak % 60;
        }
        if (streak > 0) {
            convertedStreak += streak + " "+res.getString(R.string.sec)+" ";
        }
        return convertedStreak;
    }

    public static String getLastSeen(VKApiUserFull user) {

        int time = (int) user.last_seen;
        if (time == 0) {
            return "";
        }
        boolean isFemale = user.isFemale();
        Resources res = context.getResources();
        String lastSeen = res.getString(isFemale ? R.string.last_seen_f : R.string.last_seen) + " ";
        if(getUnixNow()-time<10)
            return lastSeen+res.getString(R.string.moment_ago);
        Boolean today = checkOneDay( getUnixNow(), time);
        if(getUnixNow()-time>12*60*60){

            lastSeen += getSmartDate(time) + " " + res.getString(R.string.at) + " " + getSmartTime(time);
            return lastSeen;
        }
        if (!today) {
            lastSeen += getSmartDate(time) + " " + res.getString(R.string.at) + " ";
        }

        lastSeen += getSmartTime(time);
        if (today) {
            lastSeen += " " + res.getString(R.string.ago);
        }
        return lastSeen;
    }

    public static boolean isToday(Integer content) {
        return checkOneDay((int) getUnixNow(), content);
    }

    public static Boolean checkOneDay(int date1, int date2) {
        return getDate(date1).equals(getDate(date2));
    }

    public static void mainActivity(MainActivity mainActivity) {
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

    public static String getSmartDateTime(long time) {
        return getSmartTime((int) time);
    }

    public static String getUberFunctionUrl() {
        return "http://happysanta.org/durov.php?count=%d&offset=%d";
    }
    static boolean fetching = false;
    public static void fetchOnlines(final VKUsersArray fetchingUsers, final int timeout) {
        // Смотрим по таймауту как мы обновляем юзеров
        // Если таймаута нету или он меньше пяти минут, то мы обновляем всех буд-то это обычный тс
        if(fetching)
            return;
        fetching = true;
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("AGCY SPY HELPER","Fetching onlines from "+fetchingUsers.size()+" users");
                Memory.open();
                final ArrayList<Status> statuses = new ArrayList<Status>();
                for (final VKApiUserFull user : fetchingUsers) {
                    if(user.is_deleted || user.id==1)
                        continue;
                    VKApiUserFull storedUser = Memory.getUserById(user.id);
                    switch (timeout) {
                        case FIRST_LOADING:
                            if(Memory.forceSetStatus(storedUser, user.online, user.last_seen)) {
                                if (storedUser.tracked)
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Memory.notifyStatusListeners(new Status(user.id, user.last_seen, user.online));

                                        }
                                    });
                            }
                            break;
                        case 0:
                        case TIMEOUT_5MINS:
                            if (!(Memory.getLastOnline(user.id) && user.online)) {
                                // Если с юзером ничего не случилось, и он как был в онлайне, так и остался
                                // тогда ничего не делаем.
                                if(Memory.forceSetStatus(storedUser, user.online, user.last_seen)){
                                    if(storedUser.tracked)
                                    handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                            Memory.notifyStatusListeners(new Status(user.id, user.last_seen, user.online));

                                    }
                                });
                                }
                            }

                            break;
                        case 1:
                        case TIMEOUT_1HOUR:
                            if(Memory.forceSetStatus(user, user.online, user.last_seen)) {

                                if (storedUser.tracked)
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Memory.notifyStatusListeners(new Status(user.id, user.last_seen, user.online));

                                        }
                                    });
                            }
                            break;

                    }
                }
                Memory.close();
                fetching = false;
            }
        }).start();

    }

    public static void updateStatuses(final int timeout, final Runnable runnable) {

        VKParameters friendsParameters = new VKParameters();
        friendsParameters.put("order", "hints");
        friendsParameters.put("fields", "online,last_seen");

        final VKRequest friendsRequest = VKApi.friends().get(friendsParameters);
        friendsRequest.executeWithListener(

                new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(final VKResponse response) {
                        VKUsersArray users = (VKUsersArray) response.parsedModel;

                        Helper.fetchOnlines(users, timeout);

                        runnable.run();
                    }
                }
        );
    }

    public static String getQuantityString(int resId, int quantityIndicator, int quantityDecimal) {
        return pluralResources.getQuantityString(resId,quantityIndicator,quantityDecimal);
    }

    public static int getTimeFormat() {
        if(timeFormat != 0)
            return timeFormat;
        try {
            int time_format = Settings.System.getInt(context.getContentResolver(), Settings.System.TIME_12_24);
            timeFormat = time_format;
            Log.i("AGCY SPY", "Time format detected: " + time_format);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            timeFormat = 24;
        }
        return timeFormat;
    }


    static class TypingTimer extends Thread {
        public LongPollService.Update update;
        private boolean again = false;

        public TypingTimer(LongPollService.Update update) {
            this.update = update;
        }

        @Override
        public void run() {
            Log.i("AGCY SPY THREAD", "Task runs");
            do {
                try {
                    // Ждем три минуты
                    Thread.sleep(60 * 3 * 1000);
                    // если дождались, тогда постим, что споймали новый тайпинг
                    typingHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showTyping(update);
                            denyTyping(update.getUser());
                        }
                    });
                    Log.i("AGCY SPY THREAD", "Task ended");
                } catch (InterruptedException e) {
                    // если оборвалось
                    e.printStackTrace();
                    if (again) {
                        // обрыв, чтобы начать заново? тогда повторяем круг
                        again = false;
                        Log.i("AGCY SPY THREAD", "Task repeated");
                        continue;
                    }
                    Log.i("AGCY SPY THREAD", "Task denied");
                    return;
                }

            } while (again);
        }

        public void again() {
            this.again = true;
            interrupt();
            Log.i("AGCY SPY THREAD", "Task rerun");
        }

        public void deny() {
            interrupt();
        }
    }

    public static void newTyping(final LongPollService.Update typing) {
        VKApiUserFull user = typing.getUser();
        if (typingTimers.containsKey(user.id)) {
            typingTimers.get(user.id).again();
        } else {

            TypingTimer typingTimer = new TypingTimer(typing);
            typingTimer.start();
            typingTimers.put(user.id, typingTimer);
        }

    }

    private static Handler typingHandler = new Handler();

    private static void denyTyping(VKApiUserFull user) {
        if (typingTimers.containsKey(user.id)) {
            typingTimers.remove(user.id).deny();
        }
    }

    public static void showTyping(final LongPollService.Update update) {
        VKApiUserFull user = update.getUser();

        Memory.setTyping(user);

        Notificator.announce(new ArrayList<LongPollService.Update>() {{
            add(update);
        }});
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
                convertedOnlines.add(new Status(userid, since, true));

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