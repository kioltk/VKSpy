package com.agcy.vkproject.spy.Core;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.MainActivity;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.SpyApplication;
import com.agcy.vkproject.spy.UserActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;
import java.util.List;

public class Notificator {

    private static final int ONLINES_NOTIFICATION = -1;
    private static final int TICKER = -3;
    private static Context context;
    private static int OFFLINES_NOTIFICATION = -2;
    public static boolean onlinesOpened = false;

    public static void initialize(Context context) {
        Notificator.context = context;
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
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        Log.d("topActivity", "CURRENT Activity ::"
                + taskInfo.get(0).topActivity.getClassName());

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(context.getPackageName());
    }

    /**
     * @return Returns null if should notify at all. False if it's simple notification and true if it's popup (toast).
     */
    public static Boolean checkShowPopup(LongPollService.Update update) {


        SharedPreferences notificationPreferences = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

        boolean enabled = notificationPreferences.getBoolean("status", true);
        if (!enabled)
            return null;
        switch (update.getType()) {
            case LongPollService.Update.TYPE_OFFLINE:
                if (!Memory.isTracked(update.getUser()) || onlinesOpened)
                    return null;

                boolean enabledOffline = notificationPreferences.getBoolean("notificationOffline", true);
                if (!enabledOffline)
                    return null;

                return notificationPreferences.getInt("wayToNotifyOffline", 0) > 0;

            case LongPollService.Update.TYPE_ONLINE:
                if (!Memory.isTracked(update.getUser()) || onlinesOpened)
                    return null;

                boolean enabledOnline = notificationPreferences.getBoolean("notificationOffline", true);
                if (!enabledOnline)
                    return null;

                return notificationPreferences.getInt("wayToNotifyOnline", 0) > 0;

            case LongPollService.Update.TYPE_USER_TYPING:
                return false;
            case LongPollService.Update.TYPE_CHAT_TYPING:
            default:
                return null;
        }


    }
    /**
     * these methods are not needed now
    public static Boolean updateShouldPopup(LongPollService.Update update) {

        SharedPreferences prefs = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
        switch (update.getType()) {
            case LongPollService.Update.TYPE_OFFLINE:
            case LongPollService.Update.TYPE_ONLINE:
                return Memory.isTracked(update.getUser());
            case LongPollService.Update.TYPE_CHAT_TYPING:
            case LongPollService.Update.TYPE_USER_TYPING:
            default:
                return false;
        }
    }

    public static Boolean updateShouldNotify(LongPollService.Update update) {

        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);


        switch (update.getType()) {
            case LongPollService.Update.TYPE_OFFLINE:
                boolean offlineNotify = prefs.getBoolean("notificationOffline", true);
                if (!offlineNotify)
                    return false;
                return update.getUser().isTracked();
            case LongPollService.Update.TYPE_ONLINE:

                boolean onlineNotify = prefs.getBoolean("notificationOnline", true);
                if (!onlineNotify)
                    return false;
                return update.getUser().isTracked();
            case LongPollService.Update.TYPE_USER_TYPING:
                return true;
            case LongPollService.Update.TYPE_CHAT_TYPING:
            default:
                return false;
        }
    }
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
            showPopup(lastOnlineEvent,onlinesPopup);
        }
        if (lastOfflineEvent != null) {
            showPopup(lastOfflineEvent,offlinesPopup);
        }
    }

    private static void showPopup(Event lastEvent, int countEvents) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.popup, null);

        final Toast toast = new Toast(context);


        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        titleView.setText(lastEvent.headerText);
        TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
        if(countEvents==1){
            descriptionView.setText(lastEvent.messageText);
        }else{
            descriptionView.setText(Helper.getQuantityString(
                    lastEvent.getType() == LongPollService.Update.TYPE_OFFLINE ?
                            R.plurals.offlines_notification : R.plurals.onlines_notification,
                    countEvents - 1,
                    countEvents - 1
            ));
        }
        final ImageView photoView = (ImageView) rootView.findViewById(R.id.photo);
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
                photoView.setImageBitmap(bitmap);
                toast.show();
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    public static void clearNotifications(){
        if(context!=null) {
            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }

        onlinesNotification.clear();
        offlinesNotification.clear();
        offlinePhotosStack.clear();
        onlinePhotosStack.clear();
    }
    private static ArrayList<String> onlinePhotosStack = new ArrayList<String>();
    private static ArrayList<String> offlinePhotosStack = new ArrayList<String>();
    private static ArrayList<String> onlinesNotification = new ArrayList<String>();
    private static ArrayList<String> offlinesNotification = new ArrayList<String>();
    public static void showNotification(ArrayList<Event> notifyEvents) {

        if (notifyEvents.isEmpty())
            return;

        Event lastOnlineEvent = null;
        Event lastOfflineEvent = null;
        for (Event notifyEvent : notifyEvents) {
            switch (notifyEvent.getType()) {
                case LongPollService.Update.TYPE_ONLINE:
                    lastOnlineEvent = notifyEvent;
                    putImageToStack(notifyEvent.imageUrl, onlinePhotosStack);
                    onlinesNotification.remove(notifyEvent.getShortMessage());
                    onlinesNotification.add(0, notifyEvent.getShortMessage());
                    break;

                case LongPollService.Update.TYPE_OFFLINE:
                    lastOfflineEvent = notifyEvent;
                    putImageToStack(notifyEvent.imageUrl, offlinePhotosStack);
                    offlinePhotosStack.remove(notifyEvent.getShortMessage());
                    offlinesNotification.add(0, notifyEvent.getShortMessage());
                    break;
                case LongPollService.Update.TYPE_USER_TYPING:
                    showSingleNotification(notifyEvent);
                    break;

            }
        }
        if ((onlinesNotification.size()>1) && lastOnlineEvent != null) {
            showOnlines(lastOnlineEvent);
        } else {
            if (lastOnlineEvent != null) {
                showSingleNotification(lastOnlineEvent);
            }
        }
        if ((offlinesNotification.size()>1) && lastOfflineEvent != null) {
            showOfflines(lastOfflineEvent);
        } else {
            if (lastOfflineEvent != null) {
                showSingleNotification(lastOfflineEvent);
            }
        }


    }
    static void showOfflines(final Event lastOnlineEvent) {

        final String offlineHeaderText = lastOnlineEvent.headerText;
        final String offlineMessageText = lastOnlineEvent.messageText;
        String offlinesSummary = Helper.getQuantityString(R.plurals.offlines_notification, offlinesNotification.size() - 1, offlinesNotification.size() - 1);
        String imageUrl = lastOnlineEvent.imageUrl;
        final String finalSummary = offlinesSummary;

        boolean tickerOnly = applicationIsOpened();
        if(tickerOnly){
            showTicker(offlineHeaderText +" "+ finalSummary.toLowerCase());
            return;
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);
        if(!applicationIsOpened())
        notificationBuilder
                        .setContentTitle(offlineHeaderText)
                        .setAutoCancel(true)
                        .setContentText(finalSummary)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(offlineHeaderText + " " + offlineMessageText.toLowerCase())
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.ic_stat_spy);

        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

        boolean soundEnabled = prefs.getBoolean("sound", true);
        if (soundEnabled)
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        boolean vibrateOn = prefs.getBoolean("vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Intent intent = new Intent(context, ClearNotificationsReceiver.class);
        intent.setAction(ClearNotificationsReceiver.ACTION_CLEAR);
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

    private static void showTicker(String tickerText){

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_stat_spy)
                .setDefaults(Notification.DEFAULT_LIGHTS);
        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);
        boolean vibrateOn = prefs.getBoolean("vibrate", true);

        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Notification notification = notificationBuilder.build();
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(TICKER, notification);
        clearNotifications();
    }

    static void showOnlines(final Event lastOnlineEvent) {
        final String onlineHeaderText = lastOnlineEvent.headerText;
        final String onlineMessageText = lastOnlineEvent.messageText;
        String onlineSummaryText = Helper.getQuantityString(R.plurals.onlines_notification, onlinesNotification.size() - 1, onlinesNotification.size() - 1);
        String imageUrl = lastOnlineEvent.imageUrl;

        final String finalSummary = onlineSummaryText;
        boolean tickerOnly = applicationIsOpened();
        if(tickerOnly){
            showTicker(onlineHeaderText +" "+ finalSummary.toLowerCase());
            return;
        }
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);

        if(!applicationIsOpened())
            notificationBuilder
                        .setContentTitle(onlineHeaderText)
                        .setContentText(finalSummary)
                        .setAutoCancel(true)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(lastOnlineEvent.headerText + " " + lastOnlineEvent.messageText.toLowerCase())
        .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.ic_stat_spy);
        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

        boolean soundEnabled = prefs.getBoolean("sound", true);
        if (soundEnabled)
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        boolean vibrateOn = prefs.getBoolean("vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});

        Intent intent = new Intent(context, ClearNotificationsReceiver.class);
        intent.setAction(ClearNotificationsReceiver.ACTION_CLEAR);
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
        if(tickerOnly){
            showTicker(event.headerText +" "+ event.messageText.toLowerCase());
            return;
        }
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context);


            notificationBuilder
                        .setContentTitle(event.headerText)
                        .setContentText(event.messageText)
                        .setAutoCancel(true)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(event.headerText + " " + event.messageText)
                .setSmallIcon(R.drawable.ic_stat_spy)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

        boolean soundEnabled = prefs.getBoolean("sound", true);
        if (soundEnabled)
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        boolean vibrateOn = prefs.getBoolean("vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});


        int id ;
        Intent resultIntent;

        if (event.getType() == LongPollService.Update.TYPE_USER_TYPING) {
            id = event.id;
            resultIntent = new Intent(context, MainActivity.class);
        }else{
            resultIntent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", event.id);
            resultIntent.putExtras(bundle);
            if(event.getType()== LongPollService.Update.TYPE_ONLINE) {
                id = ONLINES_NOTIFICATION;
            }
            else {
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
    private static Boolean putImageToStack(String url, ArrayList<String> imagesStack) {
        Boolean exists = false;
        if (!imagesStack.remove(url)) {
            if (imagesStack.size() == 3)
                imagesStack.remove(2);
            exists= true;
        }
        imagesStack.add(0, url);
        return exists;
    }
    private static Bitmap getBitmap(ArrayList<String> urlStack) {
        try {


            int height = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width);

            ArrayList<Bitmap> imagesStack = new ArrayList<Bitmap>();
            for (String url : urlStack) {
                imagesStack.add(ImageLoader.getInstance().loadImageSync(url));
            }
            ;

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
        Event event = new Event(durov.first_name+" "+ durov.last_name, context.getString(R.string.come_online_m) ,
                durov.getBiggestPhoto(), 1 );
        showOnlines(event);
    }

    public static class ClearNotificationsReceiver extends BroadcastReceiver {
        public static final String ACTION_CLEAR = "AGCY_SPY_NOTIFICATIONS_CANCEL";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_CLEAR)) {
                clearNotifications();
            }
        }
    }

public static class Event {

        private int id;
        public String headerText;
        public String messageText;
        public String imageUrl;
        private String shortMessage;
        private int type;

        public Event(String headerText, String messageText, String imageUrl, int id) {
            this.headerText = headerText;
            this.messageText = messageText;
            this.imageUrl = imageUrl;

            this.id = id;
        }

        public Event(LongPollService.Update update) {

            this.headerText = update.getHeader();
            this.messageText = update.getMessage();
            this.shortMessage = update.getShortMessage();
            this.imageUrl = update.getImageUrl();
            this.type = update.getType();
            this.id = update.getUser().id;
        }




        public String getShortMessage() {
            return shortMessage;
        }

        public int getType() {
            return type;
        }
    }
}