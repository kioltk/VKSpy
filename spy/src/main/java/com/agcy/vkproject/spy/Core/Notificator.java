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
import com.agcy.vkproject.spy.UserActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class Notificator {

    private static final int ONLINES_NOTIFICATION = -1;
    private static Context context;
    private static int OFFLINES_NOTIFICATION = -2;

    public static void initialize(Context context) {
        Notificator.context = context;
    }

    public static void announce(ArrayList<LongPollService.Update> updates) {
        if (updates.size() == 0)
            return;
        if (applicationIsOpened()) {
            return;
        }
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
        return false;//componentInfo.getPackageName().equals(context.getPackageName());
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
                if (!Memory.isTracked(update.getUser()))
                    return null;

                boolean enabledOffline = notificationPreferences.getBoolean("notificationOffline", true);
                if (!enabledOffline)
                    return null;

                return notificationPreferences.getInt("wayToNotifyOffline", 0) > 0;

            case LongPollService.Update.TYPE_ONLINE:
                if (!Memory.isTracked(update.getUser()))
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


        Toast toast = new Toast(context);


        LinearLayout rootView = new LinearLayout(context);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setBackgroundResource(R.drawable.popup_message);

        for (Event event : events) {
            rootView.addView(event.getView(context));
        }

        toast.setView(rootView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void clearNotifications(){

        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        onlinesNotification.clear();
        offlinesNotification.clear();
        offlinePhotosStack.clear();
        onlinePhotosStack.clear();
    }
    private static ArrayList<Bitmap> onlinePhotosStack = new ArrayList<Bitmap>();
    private static ArrayList<Bitmap> offlinePhotosStack = new ArrayList<Bitmap>();
    private static ArrayList<String> onlinesNotification = new ArrayList<String>();
    private static ArrayList<String> offlinesNotification = new ArrayList<String>();
    public static void showNotification(ArrayList<Event> notifyEvents) {

        if (notifyEvents.isEmpty())
            return;

        Event lastOnlineEvent = null;
        Event lastOfflineEvent = null;
        if (onlinesNotification.isEmpty() && offlinesNotification.isEmpty() && notifyEvents.size() == 1) {
            showSingleNotification(notifyEvents.get(0));
            return;
        } else {
            for (Event notifyEvent : notifyEvents) {
                if (notifyEvent.getType() == LongPollService.Update.TYPE_ONLINE) {
                    lastOnlineEvent = notifyEvent;
                    onlinesNotification.add(0, notifyEvent.getShortMessage());
                } else {
                    lastOfflineEvent = notifyEvent;
                    offlinesNotification.add(0, notifyEvent.getShortMessage());
                }
            }
        }
        if(!onlinesNotification.isEmpty() && lastOnlineEvent!=null) {
            showOnlines(lastOnlineEvent);
        }
        if(!offlinesNotification.isEmpty() && lastOfflineEvent!=null){
            showOfflines(lastOfflineEvent);
        }


    }
    static void showOfflines(final Event lastOnlineEvent) {

        final String offlineHeaderText = lastOnlineEvent.headerText;
        final String offlineMessageText = lastOnlineEvent.messageText;
        String offlinesSummary = context.getResources().getQuantityString(R.plurals.offlines_notification, offlinesNotification.size(), offlinesNotification.size());
        String imageUrl = lastOnlineEvent.imageUrl;
        final String finalSummary = offlinesSummary;

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_spy)
                        .setContentTitle(offlineHeaderText)
                        .setAutoCancel(true)
                        .setContentText(finalSummary)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(lastOnlineEvent.headerText + " " + lastOnlineEvent.messageText.toLowerCase());

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
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                try {
                    notificationBuilder.setLargeIcon(getImage(bitmap, offlinePhotosStack));
                } catch (Exception exp) {

                }
                Notification notification = notificationBuilder.build();
                mNotificationManager.notify(mId, notification);


            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }
    static void showOnlines(final Event lastOnlineEvent) {
        final String onlineHeaderText = lastOnlineEvent.headerText;
        final String onlineMessageText = lastOnlineEvent.messageText;
        String onlineSummaryText = context.getResources().getQuantityString(R.plurals.onlines_notification, onlinesNotification.size(), onlinesNotification.size());
        String imageUrl = lastOnlineEvent.imageUrl;

        final String finalSummary = onlineSummaryText;

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_spy)
                        .setContentTitle(onlineHeaderText)
                        .setContentText(finalSummary)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(lastOnlineEvent.headerText + " " + lastOnlineEvent.messageText.toLowerCase());

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
        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
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
                try {
                    notificationBuilder.setLargeIcon(getImage(bitmap,onlinePhotosStack));
                } catch (Exception exp) {

                }
                Notification notification = notificationBuilder.build();
                mNotificationManager.notify(mId, notification);


            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }
    public static void showSingleNotification(Event event) {

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_spy)
                        .setContentTitle(event.headerText)
                        .setContentText(event.messageText)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentInfo("VK Spy");
        notificationBuilder.setTicker(event.headerText + " " + event.messageText);

        SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

        boolean soundEnabled = prefs.getBoolean("sound", true);
        if (soundEnabled)
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        boolean vibrateOn = prefs.getBoolean("vibrate", true);
        if (vibrateOn)
            notificationBuilder.setVibrate(new long[]{0, 50, 200, 50});


        int id ;
        Intent resultIntent;

        if (event.getType()== LongPollService.Update.TYPE_USER_TYPING) {
            id = event.id;
            resultIntent = new Intent(context, MainActivity.class);
        }else{
            resultIntent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", event.id);
            resultIntent.putExtras(bundle);
            if(event.getType()== LongPollService.Update.TYPE_ONLINE) {
                onlinesNotification.add(0, event.getShortMessage());
                id = ONLINES_NOTIFICATION;
            }
            else {
                offlinesNotification.add(0, event.getShortMessage());
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
                try {
                    notificationBuilder.setLargeIcon(getImage(bitmap,mId==ONLINES_NOTIFICATION? onlinePhotosStack:offlinePhotosStack));
                } catch (Exception exp) {

                }
                Notification notification = notificationBuilder.build();
                mNotificationManager.notify(mId, notification);

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }

    private static Bitmap getImage(Bitmap newBitmap, ArrayList<Bitmap> imagesStack) {


        int height = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width);

        imagesStack.add(0,newBitmap);
        if(imagesStack.size()>3)
            imagesStack.remove(3);


        if(imagesStack.size()==1){
            newBitmap = Bitmap.createScaledBitmap(newBitmap, width, height, true);
            return newBitmap;
        }
        if(imagesStack.size()==2)
        {

            newBitmap = Bitmap.createScaledBitmap(newBitmap, width, height, true);
            newBitmap = Bitmap.createBitmap(newBitmap,width/4,0,width/2,height);

            Bitmap anotherBitmap = Bitmap.createScaledBitmap(imagesStack.get(1),width,height,true);
            anotherBitmap = Bitmap.createBitmap(anotherBitmap,width/4,0,width/2,height);



            Canvas comboImage = new Canvas();
            Bitmap tempBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            comboImage.setBitmap(tempBitmap);
            comboImage.drawBitmap(newBitmap,0,0,null);
            comboImage.drawBitmap(anotherBitmap,width/2,0,null);
            comboImage.setBitmap(null);
            return tempBitmap;
        }
        //if(imagesStack.size()==3)
        {

            newBitmap = Bitmap.createScaledBitmap(newBitmap, width, height, true);
            newBitmap = Bitmap.createBitmap(newBitmap,width/4,0,width/2,height);

            Bitmap anotherBitmap = Bitmap.createScaledBitmap(imagesStack.get(1),width/2,height/2,true);
            Bitmap anotherBitmap2 = Bitmap.createScaledBitmap(imagesStack.get(2), width/2, height/2, true);


            Canvas comboImage = new Canvas();
            Bitmap tempBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            comboImage.setBitmap(tempBitmap);
            comboImage.drawBitmap(newBitmap,0,0,null);
            comboImage.drawBitmap(anotherBitmap,width/2,0,null);
            comboImage.drawBitmap(anotherBitmap2,width/2,height/2,null);
            return tempBitmap;
        }


    }



    public static void DESTROY() {
        context = null;
    }

    public static void exampleNotify() {

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


        private View getView(Context context) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = inflater.inflate(R.layout.popup, null);

            ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            TextView header = (TextView) rootView.findViewById(R.id.header);
            TextView message = (TextView) rootView.findViewById(R.id.message);
            header.setText(headerText);
            message.setText(messageText);
            ImageLoader.getInstance().displayImage(imageUrl, photo);
            return rootView;
        }

        public String getShortMessage() {
            return shortMessage;
        }

        public int getType() {
            return type;
        }
    }
}