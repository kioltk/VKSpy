package com.happysanta.vkspy.Core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.happysanta.vkspy.Garbage.MagicBounceInterpolator;
import com.happysanta.vkspy.Longpoll.LongPollService;
import com.happysanta.vkspy.MainActivity;
import com.happysanta.vkspy.R;
import com.happysanta.vkspy.UserActivity;
import com.bugsense.trace.BugSenseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;
import java.util.HashMap;

public class Notificator {

    private static final int ONLINES_NOTIFICATION = -1;
    private static final int TICKER = -3;
    private static Context context;
    private static int OFFLINES_NOTIFICATION = -2;
    public static boolean onlinesOpened = false;
    private static SharedPreferences notificationsPreferences;

    public static void initialize(Context context) {
        Notificator.context = context;
        notificationsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void announce(ArrayList<LongPollService.Update> updates) {
        if (updates.size() == 0)
            return;

        ArrayList<Event> popupEvents = new ArrayList<Event>();
        ArrayList<Event> notifyEvents = new ArrayList<Event>();
        for (LongPollService.Update update : updates) {
            Boolean checkShowPopup = checkShowPopup(update);
            if (checkShowPopup == null)
                continue;
            if (checkShowPopup)
                popupEvents.add(new Event(update));
            else {
                if (update.getType() != LongPollService.Update.TYPE_USER_TYPING)
                    notifyEvents.add(new Event(update));
                else
                    showSingleNotification(new Event(update));
            }
        }
        showPopup(popupEvents);
        showNotification(notifyEvents);

    }

    private static boolean applicationIsOpened() {
        return MainActivity.isOpened;
    }

    /**
     * @return Returns null if should notify at all. False if it's simple notification and true if it's popup (toast).
     */
    public static Boolean checkShowPopup(LongPollService.Update update) {


        boolean enabled = notificationsPreferences.getBoolean("notifications_enabled", true);
        if (!enabled)
            return null;
        switch (update.getType()) {
            case LongPollService.Update.TYPE_OFFLINE:
                if (!Memory.isTracked(update.getUserId()) || onlinesOpened)
                    return null;

                boolean enabledOffline = notificationsPreferences.getBoolean("notifications_offline_enabled", true);
                if (!enabledOffline)
                    return null;

                return notificationsPreferences.getString("notifications_offline_type", "1").equals("1");

            case LongPollService.Update.TYPE_ONLINE:
                if (!Memory.isTracked(update.getUserId()) || onlinesOpened)
                    return null;

                boolean enabledOnline = notificationsPreferences.getBoolean("notifications_online_enabled", true);
                if (!enabledOnline)
                    return null;
                return notificationsPreferences.getString("notifications_offline_type", "1").equals("1");

            case LongPollService.Update.TYPE_CHAT_TYPING:
                boolean multichatEnabled = notificationsPreferences.getBoolean("notifications_chat_typing_enabled", true);
                return multichatEnabled ? false : null;
            case LongPollService.Update.TYPE_USER_TYPING:
                boolean typingEnabled = notificationsPreferences.getBoolean("notifications_typing_enabled", true);
                return typingEnabled ? false : null;
            default:
                return null;
        }


    }

    /**
     * these methods are not needed now
     * public static Boolean updateShouldPopup(LongPollService.Update update) {
     * <p/>
     * SharedPreferences prefs = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
     * switch (update.getType()) {
     * case LongPollService.Update.TYPE_OFFLINE:
     * case LongPollService.Update.TYPE_ONLINE:
     * return Memory.isTracked(update.getUser());
     * case LongPollService.Update.TYPE_CHAT_TYPING:
     * case LongPollService.Update.TYPE_USER_TYPING:
     * default:
     * return false;
     * }
     * }
     * <p/>
     * public static Boolean updateShouldNotify(LongPollService.Update update) {
     * <p/>
     * SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);
     * <p/>
     * <p/>
     * switch (update.getType()) {
     * case LongPollService.Update.TYPE_OFFLINE:
     * boolean offlineNotify = prefs.getBoolean("notificationOffline", true);
     * if (!offlineNotify)
     * return false;
     * return update.getUser().isTracked();
     * case LongPollService.Update.TYPE_ONLINE:
     * <p/>
     * boolean onlineNotify = prefs.getBoolean("notificationOnline", true);
     * if (!onlineNotify)
     * return false;
     * return update.getUser().isTracked();
     * case LongPollService.Update.TYPE_USER_TYPING:
     * return true;
     * case LongPollService.Update.TYPE_CHAT_TYPING:
     * default:
     * return false;
     * }
     * }
     */

    public static void showPopup(ArrayList<Event> events) {
        if (events.isEmpty())
            return;
        int offlinesPopup = 0;
        int onlinesPopup = 0;
        Event lastOnlineEvent = null;
        Event lastOfflineEvent = null;
        for (Event popupEvent : events) {
            switch (popupEvent.getType()) {
                case LongPollService.Update.TYPE_ONLINE:
                    lastOnlineEvent = popupEvent;
                    onlinesPopup++;
                    break;

                case LongPollService.Update.TYPE_OFFLINE:
                    lastOfflineEvent = popupEvent;
                    offlinesPopup++;
                    break;

            }
        }
        if (lastOnlineEvent != null) {
            showPopup(lastOnlineEvent, onlinesPopup);
        }
        if (lastOfflineEvent != null) {
            showPopup(lastOfflineEvent, offlinesPopup);
        }
    }

    private static void showPopup(Event lastEvent, int countEvents) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.popup, null);

        final Toast toast = new Toast(context);


        final TextView titleView = (TextView) rootView.findViewById(R.id.title);
        titleView.setText(lastEvent.headerText);
        final TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
        if (countEvents == 1) {
            descriptionView.setText(lastEvent.messageText);
        } else {
            descriptionView.setText(Helper.getQuantityString(
                    lastEvent.getType() == LongPollService.Update.TYPE_OFFLINE ?
                            R.plurals.offlines_notification : R.plurals.onlines_notification,
                    countEvents - 1,
                    countEvents - 1
            ));
        }
        final ImageView photoHolder = (ImageView) rootView.findViewById(R.id.photo);
        toast.setView(rootView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);

        ImageLoader.getInstance().loadImage(lastEvent.imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                photoHolder.setImageBitmap(bitmap);
                toast.show();



                ScaleAnimation blinkAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, 0.5f);
                blinkAnimation.setInterpolator(new MagicBounceInterpolator());
                blinkAnimation.setDuration(1200);
                blinkAnimation.setStartOffset(00);
                photoHolder.startAnimation(blinkAnimation);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);

                TranslateAnimation translateAnimation = new TranslateAnimation(200,0,0,0);

                AnimationSet slideInAnimation = new AnimationSet(true);
                slideInAnimation.setInterpolator(new DecelerateInterpolator());
                slideInAnimation.setDuration(400);
                slideInAnimation.setStartOffset(100);

                slideInAnimation.addAnimation(alphaAnimation);
                slideInAnimation.addAnimation(translateAnimation);

                titleView.startAnimation(slideInAnimation);
                descriptionView.startAnimation(slideInAnimation);

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public static void clearOfflines(){

        offlinePhotosStack.clear();
        offlinesNotification.clear();
    }
    public static void clearOnlines(){
        onlinePhotosStack.clear();
        onlinesNotification.clear();
    }
    public static void clearChat(int chatid){

        ArrayList<Integer> chatNotification = chatsNotifications.get(chatid);
        if(chatNotification!=null)
            chatNotification.clear();

        ArrayList<String> chatPhotoStack = chatsPhotosStacks.get(chatid);
        if(chatPhotoStack!=null)
            chatPhotoStack.clear();
    }

    public static void clearNotifications() {
        if (context != null) {
            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }
        clearOfflines();
        clearOnlines();

        chatsNotifications.clear();
        chatsPhotosStacks.clear();
    }
    // меньше ООП = больше гемора. Большее ООП = больше гемора. ПИКАЧУ, Я ВЫБИРАЮ ТЕБЯ!
    // адреса картинок
    private static ArrayList<String> onlinePhotosStack = new ArrayList<String>();
    private static ArrayList<String> offlinePhotosStack = new ArrayList<String>();
    private static HashMap<Integer,ArrayList<String>> chatsPhotosStacks = new HashMap<Integer, ArrayList<String>>();
    // integer - айди юзеров
    private static ArrayList<Integer> onlinesNotification = new ArrayList<Integer>();
    private static ArrayList<Integer> offlinesNotification = new ArrayList<Integer>();
    private static HashMap<Integer,ArrayList<Integer>> chatsNotifications = new HashMap<Integer, ArrayList<Integer>>();
    public static void showNotification(ArrayList<Event> notifyEvents) {

        if (notifyEvents.isEmpty())
            return;
        Integer indexIfExists = null;
        Event lastOnlineEvent = null;
        Event lastOfflineEvent = null;
        HashMap<Integer,Event> lastChatEvents = new HashMap<Integer, Event>();
        for (Event notifyEvent : notifyEvents) {
            switch (notifyEvent.getType()) {
                case LongPollService.Update.TYPE_ONLINE:
                    lastOnlineEvent = notifyEvent;
                    indexIfExists = onlinesNotification.indexOf(notifyEvent.getUserId());
                    putImageToStack(notifyEvent.imageUrl, onlinePhotosStack, indexIfExists);
                    onlinesNotification.remove(notifyEvent.getUserId());
                    onlinesNotification.add(0, notifyEvent.getUserId());
                    break;

                case LongPollService.Update.TYPE_OFFLINE:
                    lastOfflineEvent = notifyEvent;
                    indexIfExists = offlinesNotification.indexOf(notifyEvent.getUserId());
                    putImageToStack(notifyEvent.imageUrl, offlinePhotosStack, indexIfExists);
                    offlinesNotification.remove(notifyEvent.getUserId());
                    offlinesNotification.add(0, notifyEvent.getUserId());
                    break;
                case LongPollService.Update.TYPE_USER_TYPING:
                    showSingleNotification(notifyEvent);
                    break;
                case LongPollService.Update.TYPE_CHAT_TYPING:
                    int chatid = (Integer) notifyEvent.getExtra();
                    lastChatEvents.put(chatid, notifyEvent);
                    ArrayList<String> chatPhotoStack;
                    ArrayList<Integer> chatNotifications;
                    if (!chatsNotifications.containsKey(chatid)){
                        chatsPhotosStacks.put(chatid, new ArrayList<String>());
                        chatsNotifications.put(chatid, new ArrayList<Integer>());
                    }
                    chatPhotoStack = chatsPhotosStacks.get(chatid);
                    chatNotifications = chatsNotifications.get(chatid);


                    indexIfExists = chatNotifications.indexOf(notifyEvent.getUserId());

                    putImageToStack(notifyEvent.imageUrl, chatPhotoStack, indexIfExists);
                    chatNotifications.remove(notifyEvent.getUserId());
                    chatNotifications.add(0, notifyEvent.getUserId());
                    break;

            }
        }
        if(!lastChatEvents.isEmpty()) {
            showChatTypings(lastChatEvents);
        }
        if ((onlinesNotification.size() > 1) && lastOnlineEvent != null) {
            showOnlines(lastOnlineEvent);
        } else {
            if (lastOnlineEvent != null) {
                showSingleNotification(lastOnlineEvent);
            }
        }
        if ((offlinesNotification.size() > 1) && lastOfflineEvent != null) {
            showOfflines(lastOfflineEvent);
        } else {
            if (lastOfflineEvent != null) {
                showSingleNotification(lastOfflineEvent);
            }
        }


    }

    private static void showChatTypings(HashMap<Integer, Event> lastChatEvents) {
        for (Event event : lastChatEvents.values()) {
            showChatTyping(event);
        }
    }
    static void showNotificationExample(Event event){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_spy)
                        .setContentInfo("contentInfo")
                        .setSubText("subText")

                        .setContentTitle(context.getString(R.string.notification))
                        .setContentText(context.getString(R.string.no_events))
                        .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
        /*
         * Sets the big view "big text" style and supplies the
         * text (the user's reminder message) that will be displayed
         * in the detail area of the expanded notification.
         * These calls are ignored by the support library for
         * pre-4.1 devices.
         */
                        .setStyle(new NotificationCompat.BigTextStyle().setSummaryText("summary")
                                //                .bigText("ololo")
                        );
        Notification notification = builder.build();
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    static void showChatTyping(Event lastChatEvent){
        boolean tickerOnly = applicationIsOpened();
        if (tickerOnly) {
            showTicker(lastChatEvent.headerText + " " + lastChatEvent.messageText.toLowerCase());
            return;
        }

        int chatid =(Integer) lastChatEvent.getExtra();
        String chatTitle = lastChatEvent.getChatTitle();
        ArrayList<Integer> chatNotifications = chatsNotifications.get(chatid);
        final ArrayList<String> chatPhotoStack = chatsPhotosStacks.get(chatid);
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);

        String content ;
        if(chatNotifications.size()>1)
            content = Helper.getQuantityString(R.plurals.chat_notification, chatNotifications.size() - 1, chatNotifications.size() - 1);
        else
            content = lastChatEvent.getMessageText();;


        notificationBuilder
                .setContentTitle(lastChatEvent.headerText)
                .setContentText(content)
                .setSubText(chatTitle);
        notificationBuilder
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)
                        .setSummaryText(chatTitle));
        notificationBuilder
                .setAutoCancel(true)
                .setContentInfo("VK Spy");
        notificationBuilder.setTicker(lastChatEvent.headerText + " " + lastChatEvent.messageText.toLowerCase())
                .setSmallIcon(R.drawable.ic_stat_spy)
                .setDefaults(Notification.DEFAULT_LIGHTS);



        String soundUri = notificationsPreferences.getString("notifications_ringtone", "content://settings/system/notification_sound");
        if (!TextUtils.isEmpty(soundUri)) {
            notificationBuilder.setSound(Uri.parse(soundUri));
        }
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);

        boolean vibrateOn = notificationsPreferences.getBoolean("notifications_vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});


        int id = chatid;
        Intent resultIntent;

            //id = lastChatEvent.userid;
            resultIntent = new Intent(context, MainActivity.class);
        /*
        if (lastChatEvent.getType() == LongPollService.Update.TYPE_USER_TYPING) {
        } else {

            resultIntent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", lastChatEvent.userid);
            resultIntent.putExtras(bundle);
        }*/

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Intent intent = new Intent(context, ClearNotificationsReceiver.class);
        intent.setAction(ClearNotificationsReceiver.ACTION_CLEAR);
        intent.putExtra(ClearNotificationsReceiver.ACTION_CLEAR_TYPE,ClearNotificationsReceiver.ACTION_CLEAR_CHAT);
        intent.putExtra(ClearNotificationsReceiver.ACTION_CLEAR_CHAT_EXTRA, chatid);
        notificationBuilder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));


        notificationBuilder.setContentIntent(resultPendingIntent);
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.

        final int mId = id;
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap convertedBitmap = getBitmap(chatPhotoStack);
                notificationBuilder.setLargeIcon(convertedBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        Notification notification = notificationBuilder.build();
                        mNotificationManager.notify(mId, notification);

                    }
                });
            }
        }).start();


    }
    static void showOfflines(final Event lastOnlineEvent) {

        final String offlineHeaderText = lastOnlineEvent.headerText;
        final String offlineMessageText = lastOnlineEvent.messageText;
        String offlinesSummary = Helper.getQuantityString(R.plurals.offlines_notification, offlinesNotification.size() - 1, offlinesNotification.size() - 1);
        //String imageUrl = lastOnlineEvent.imageUrl;
        final String finalSummary = offlinesSummary;

        boolean tickerOnly = applicationIsOpened();
        if (tickerOnly) {
            showTicker(offlineHeaderText + " " + finalSummary.toLowerCase());
            return;
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);
        if (!applicationIsOpened())
            notificationBuilder
                    .setContentTitle(offlineHeaderText)
                    .setAutoCancel(true)
                    .setContentText(finalSummary)
                    .setContentInfo("VK Spy");
        notificationBuilder.setTicker(offlineHeaderText + " " + offlineMessageText.toLowerCase())
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.ic_stat_spy);

        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        String soundUri = notificationsPreferences.getString("notifications_ringtone", "content://settings/system/notification_sound");
        if (!TextUtils.isEmpty(soundUri)) {
            notificationBuilder.setSound(Uri.parse(soundUri));
        }
        boolean vibrateOn = notificationsPreferences.getBoolean("notifications_vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Intent intent = new Intent(context, ClearNotificationsReceiver.class);
        intent.setAction(ClearNotificationsReceiver.ACTION_CLEAR);
        intent.putExtra(ClearNotificationsReceiver.ACTION_CLEAR_TYPE,ClearNotificationsReceiver.ACTION_CLEAR_OFFLINES);
        notificationBuilder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

        Intent resultIntent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("page", MainActivity.ONLINES);
        resultIntent.putExtras(bundle);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = OFFLINES_NOTIFICATION;

        final int mId = id;
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                notificationBuilder.setLargeIcon(getBitmap(offlinePhotosStack));
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Notification notification = notificationBuilder.build();
                        mNotificationManager.notify(mId, notification);

                    }
                });
            }
        }).start();


    }

    private static void showTicker(String tickerText) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_stat_spy)
                .setDefaults(Notification.DEFAULT_LIGHTS);
        boolean vibrateOn = notificationsPreferences.getBoolean("notifications_vibrate", true);

        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Notification notification = notificationBuilder.build();
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(TICKER, notification);
        mNotificationManager.cancel(TICKER);
        clearNotifications();
    }

    static void showOnlines(final Event lastOnlineEvent) {
        final String onlineHeaderText = lastOnlineEvent.headerText;
        final String onlineMessageText = lastOnlineEvent.messageText;
        String onlineSummaryText = Helper.getQuantityString(R.plurals.onlines_notification, onlinesNotification.size() - 1, onlinesNotification.size() - 1);
        String imageUrl = lastOnlineEvent.imageUrl;

        final String finalSummary = onlineSummaryText;
        boolean tickerOnly = applicationIsOpened();
        if (tickerOnly) {
            showTicker(onlineHeaderText + " " + finalSummary.toLowerCase());
            return;
        }
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);

        if (!applicationIsOpened())
            notificationBuilder
                    .setContentTitle(onlineHeaderText)
                    .setContentText(finalSummary)
                    .setAutoCancel(true)
                    .setContentInfo("VK Spy");
        notificationBuilder.setTicker(lastOnlineEvent.headerText + " " + lastOnlineEvent.messageText.toLowerCase())
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.ic_stat_spy);

        String soundUri = notificationsPreferences.getString("notifications_ringtone", "content://settings/system/notification_sound");
        if (!TextUtils.isEmpty(soundUri)) {
            notificationBuilder.setSound(Uri.parse(soundUri));
        }
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);

        boolean vibrateOn = notificationsPreferences.getBoolean("notifications_vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Intent intent = new Intent(context, ClearNotificationsReceiver.class);
        intent.setAction(ClearNotificationsReceiver.ACTION_CLEAR);
        intent.putExtra(ClearNotificationsReceiver.ACTION_CLEAR_TYPE,ClearNotificationsReceiver.ACTION_CLEAR_ONLINES);
        notificationBuilder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

        Intent resultIntent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("page", MainActivity.ONLINES);
        resultIntent.putExtras(bundle);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = ONLINES_NOTIFICATION;

        final int mId = id;
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                notificationBuilder.setLargeIcon(getBitmap(onlinePhotosStack));
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Notification notification = notificationBuilder.build();
                        mNotificationManager.notify(mId, notification);
                    }
                });
            }
        }).start();


    }

    public static void showSingleNotification(Event event) {
        boolean tickerOnly = applicationIsOpened();
        if (tickerOnly) {
            showTicker(event.headerText + " " + event.messageText.toLowerCase());
            return;
        }
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);


        notificationBuilder
                .setContentTitle(event.headerText)
                .setContentText(event.messageText)
                .setAutoCancel(true)
                .setContentInfo("VK Spy");
        notificationBuilder.setTicker(event.headerText + " " + event.messageText.toLowerCase())
                .setSmallIcon(R.drawable.ic_stat_spy)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        String soundUri = notificationsPreferences.getString("notifications_ringtone", "content://settings/system/notification_sound");
        if (!TextUtils.isEmpty(soundUri)) {
            notificationBuilder.setSound(Uri.parse(soundUri));
        }
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);

        boolean vibrateOn = notificationsPreferences.getBoolean("notifications_vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});


        int id;
        Intent resultIntent;

        if (event.getType() == LongPollService.Update.TYPE_USER_TYPING) {
            id = event.userid;
            resultIntent = new Intent(context, MainActivity.class);
        } else {
            resultIntent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", event.userid);
            resultIntent.putExtras(bundle);
            if (event.getType() == LongPollService.Update.TYPE_ONLINE) {
                id = ONLINES_NOTIFICATION;
            } else {
                id = OFFLINES_NOTIFICATION;
            }
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        notificationBuilder.setContentIntent(resultPendingIntent);
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.

        final int mId = id;
        ImageLoader.getInstance().loadImage(event.imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {


                int height = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
                int width = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width);


                Bitmap convertedBitmap = bitmap;

                convertedBitmap = Bitmap.createScaledBitmap(convertedBitmap, width, height, true);
                notificationBuilder.setLargeIcon(convertedBitmap);

                Notification notification = notificationBuilder.build();
                mNotificationManager.notify(mId, notification);

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    private static void putImageToStack(String url, ArrayList<String> imagesStack, Integer removeAtIndex) {

        if (removeAtIndex!=null && removeAtIndex>=0 && imagesStack.size() > removeAtIndex)
            imagesStack.remove((int) removeAtIndex);
        else {
            if (imagesStack.size() == 3)
                imagesStack.remove(2);
        }

        imagesStack.add(0, url);
    }

    private static Bitmap getBitmap(ArrayList<String> urlStack) {
        try {


            int height = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width);

            ArrayList<Bitmap> imagesStack = new ArrayList<Bitmap>();
            for (String url : urlStack) {
                imagesStack.add(ImageLoader.getInstance().loadImageSync(url));
            }

            Bitmap lastImage = imagesStack.get(0);

            if (imagesStack.size() == 1) {
                lastImage = Bitmap.createScaledBitmap(lastImage, width, height, true);
                return lastImage;
            }
            if (imagesStack.size() == 2) {

                lastImage = Bitmap.createScaledBitmap(lastImage, width, height, true);
                lastImage = Bitmap.createBitmap(lastImage, width / 4, 0, width / 2, height);

                Bitmap anotherBitmap = Bitmap.createScaledBitmap(imagesStack.get(1), width, height, true);
                anotherBitmap = Bitmap.createBitmap(anotherBitmap, width / 4, 0, width / 2, height);


                Canvas comboImage = new Canvas();
                Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                comboImage.setBitmap(tempBitmap);
                comboImage.drawBitmap(lastImage, 0, 0, null);
                comboImage.drawBitmap(anotherBitmap, width / 2, 0, null);
                comboImage.setBitmap(null);
                return tempBitmap;
            }
            //if(imagesStack.size()==3)
            {

                lastImage = Bitmap.createScaledBitmap(lastImage, width, height, true);
                lastImage = Bitmap.createBitmap(lastImage, width / 4, 0, width / 2, height);

                Bitmap anotherBitmap = Bitmap.createScaledBitmap(imagesStack.get(1), width / 2, height / 2, true);
                Bitmap anotherBitmap2 = Bitmap.createScaledBitmap(imagesStack.get(2), width / 2, height / 2, true);


                Canvas comboImage = new Canvas();
                Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                comboImage.setBitmap(tempBitmap);
                comboImage.drawBitmap(lastImage, 0, 0, null);
                comboImage.drawBitmap(anotherBitmap, width / 2, 0, null);
                comboImage.drawBitmap(anotherBitmap2, width / 2, height / 2, null);
                return tempBitmap;
            }
        } catch (Exception exp) {
            BugSenseHandler.sendException(exp);
            Log.e("AGCY SPY NOTIFICATOR","creating image error",exp);
            return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.user_placeholder);
        }

    }


    public static void DESTROY() {
        context = null;
    }

    public static void exampleNotify() {

    }

    public static void notifyDurov() {
        VKApiUserFull durov = Memory.getUserById(1);
        final Event event = new Event(durov.first_name + " " + durov.last_name, context.getString(R.string.come_online_m),
                durov.getBiggestPhoto(), 1);
        onlinesNotification.add(1);
        onlinePhotosStack.add(durov.getBiggestPhoto());
        showNotification(new ArrayList<Event>() {{
            add(event);
        }});

    }

    public static boolean isInitalised() {
        return context != null && notificationsPreferences != null;
    }


    public static class ClearNotificationsReceiver extends BroadcastReceiver {
        public static final String ACTION_CLEAR = "AGCY_SPY_NOTIFICATIONS_CANCEL";
        public static final String ACTION_CLEAR_TYPE = "AGCY_SPY_NOTIFICATIONS_CANCEL_EXTRA";
        public static final int ACTION_CLEAR_ONLINES = -1;
        public static final int ACTION_CLEAR_OFFLINES = -2;
        public static final int ACTION_CLEAR_CHAT = -3;
        public static final String ACTION_CLEAR_CHAT_EXTRA = "AGCY_SPY_NOTIFICATIONS_CANCEL_CHAT_EXTRA";

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ACTION_CLEAR)) {

                int notificationType = intent.getIntExtra(ACTION_CLEAR_TYPE, 0);
                if (!Notificator.isInitalised()) {
                    Notificator.initialize(context);
                }
                switch (notificationType) {
                    case ACTION_CLEAR_OFFLINES:
                        clearOfflines();
                        break;
                    case ACTION_CLEAR_ONLINES:
                        clearOnlines();
                        break;
                    case ACTION_CLEAR_CHAT:
                        int chatid = intent.getIntExtra(ACTION_CLEAR_CHAT_EXTRA, 0);
                        clearChat(chatid);
                        break;
                }
            }

        }
    }

    public static class Event {

        private Object extra;
        private int userid;
        public String headerText;
        private String messageText;
        public String imageUrl;
        private int type;

        public Event(String headerText, String messageText, String imageUrl, int id) {
            this.headerText = headerText;
            this.messageText = messageText;
            this.imageUrl = imageUrl;

            this.userid = id;
        }

        public Event(LongPollService.Update update) {

            this.headerText = update.getHeader();
            this.messageText = update.getMessage();
            this.imageUrl = update.getImageUrl();
            this.type = update.getType();
            this.userid = update.getUserId();
            if(type == LongPollService.Update.TYPE_CHAT_TYPING)
                this.extra = update.getExtra();
        }

        public String getMessageText() {
            return messageText;
        }

        public Integer getUserId() {
            return userid;
        }
        public Object getExtra(){
            return extra;
        }
        public String getChatTitle(){
            if(extra!=null)
            return Memory.getChatById((Integer) extra).title;
            return context.getString(R.string.chat);
        }
        public int getType() {
            return type;
        }
    }
}