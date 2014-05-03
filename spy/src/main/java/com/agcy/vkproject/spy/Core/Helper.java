package com.agcy.vkproject.spy.Core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Receivers.NetworkStateReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class Helper {

    public static final int START_LOADER_ID = 150;
    public static final int START_DOWNLOADER_ID = 151;
    private static Context context;

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


    }

    public static void DESTROYALL() {

        SharedPreferences preferences = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        preferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        editor.clear();
        editor.commit();

        preferences = context.getSharedPreferences("start", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        editor.clear();
        editor.commit();

        Intent stopLongpoll = new Intent(context, LongPollService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(LongPollService.ACTION, LongPollService.ACTION_STOP);
        context.startService(stopLongpoll);
        Memory.DESTROY();
        Notificator.DESTROY();
        VKSdk.DESTROY();
        context = null;

    }


    //region Updates
    public static void newUpdates(ArrayList<LongPollService.Update> updates) {

        ArrayList<LongPollService.Update> onlines = new ArrayList<LongPollService.Update>();
        ArrayList<LongPollService.Update> messages = new ArrayList<LongPollService.Update>();
        Log.i("AGSY SPY", "New updates");
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
            Memory.setStatus(update.getUser(), (update.getType() == LongPollService.Update.TYPE_ONLINE), ((Integer) update.getExtra()) > 0);
        }
    }


    private static void newMessage(LongPollService.Update message) {
        denyTyping(message.getUser());
    }

    static HashMap<Integer, TypingTimer> typingTimers = new HashMap<Integer, TypingTimer>();

    public static void downloadUser(final int userid) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.USER_IDS, userid);
        parameters.put("fields", "sex,photo_200_orig,online,last_seen");

        VKRequest donwloadUserRequest = VKApi.users().get(parameters);
        donwloadUserRequest.executeWithListener(
                new VKRequest.VKRequestListener() {
                    public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                    @Override
                    public void onComplete(final VKResponse response) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Memory.saveUser(((VKList<VKApiUserFull>) response.parsedModel).get(0));

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                            }
                        }.execute();
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
    public static long unixNow() {
        return (System.currentTimeMillis() / 1000L);
    }

    public static String getDate(int unix) {
        Date date = new Date(unix * 1000L);

        return String.format(" %1$td %1$tB",
                date);
        //return String.format("%2$td %2$tB", date);
        //return ft.format(date);
    }

    public static String getTime(int unix) {

        Date date = new Date(unix * 1000L);


        return String.format(" %tT",
                date);
    }

    public static String getTimeShort(int unix) {

        Date date = new Date(unix * 1000L);

        return String.format(" %tR",
                date);
    }
    public static boolean isOnline(int userid){
        return Memory.getUserById(userid).online;
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
            while (true) {
                try {
                    Thread.sleep(3 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (again) {
                        again = false;
                        continue;
                    }
                    Log.i("AGCY SPY THREAD", "Task denied");
                    return;
                }
                Message showMessage = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("id", update.getUser().id);
                showMessage.setData(bundle);
                typingHandler.sendMessage(showMessage);
            }
        }

        public void again() {
            this.again = true;
            interrupt();
            Log.i("AGCY SPY THREAD", "Task rerun");
        }

        public void deny() {
            interrupt();
            //typingHandler.sendMessage(new Message( ));
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

    private static Handler typingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {

                Bundle bundle = msg.getData();
                int id = bundle.getInt("id");
                LongPollService.Update update = typingTimers.get(id).update;
                showTyping(update);
                Log.i("AGCY SPY THREAD", "Message!!1");
                denyTyping(update.getUser());

            } catch (Exception exp) {

            }
        }
    };

    private static void denyTyping(VKApiUserFull user) {
        if (typingTimers.containsKey(user.id)) {
            typingTimers.remove(user.id).deny();
        }
    }

    public static void showTyping(final LongPollService.Update update) {
        VKApiUserFull user = update.getUser();
        Memory.saveTyping(user);
        Notificator.announce(new ArrayList<LongPollService.Update>() {{
            add(update);
        }});
    }
    //endregion

    public static ArrayList<Online> convertUndefined(ArrayList<Online> onlines) {
        //Collections.sort(onlines, new CustomComparator());
        //if(!asc)
        //Collections.reverse(onlines);
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (Online online : onlines) {
            int id = online.getOwner().id;
            if (!ids.contains(id)) {
                if (online.getTill() == 0 && isOnline(id) )
                    online.setTill(-1);
                ids.add(id);
            }
        }


        return onlines;
    }

    public static class CustomComparator implements Comparator<Online> {
        @Override
        public int compare(Online o1, Online o2) {
            return o1.getUnix().compareTo(o2.getUnix());
        }
    }


}
