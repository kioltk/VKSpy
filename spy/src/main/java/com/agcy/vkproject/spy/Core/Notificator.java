package com.agcy.vkproject.spy.Core;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Notificator {

    private static Context context;

    public static void initialize(Context context) {
        Notificator.context = context;
    }

    public static void announce(ArrayList<LongPollService.Update> updates) {
        if (updates.size() == 0)
            return;
        ArrayList<Event> popupEvents = new ArrayList<Event>();
        ArrayList<Event> notifyEvents = new ArrayList<Event>();
        for (LongPollService.Update update : updates) {
            if(updateShouldPopup(update))
                popupEvents.add(new Event(update));
            if(updateShouldNotify(update)){
                notifyEvents.add(new Event(update));
            }
        }
        //showPopup(popupEvents);
        showNotification(notifyEvents);
    }



    public static Boolean updateShouldPopup(LongPollService.Update update){
        switch (update.getType()){
            case LongPollService.Update.TYPE_OFFLINE:
            case LongPollService.Update.TYPE_ONLINE:
                //return Memory.isTracked(update.getUser());
            case LongPollService.Update.TYPE_CHAT_TYPING:
            case LongPollService.Update.TYPE_USER_TYPING:
            default:
                return false;
        }
    }
    public static Boolean updateShouldNotify(LongPollService.Update update){
        switch (update.getType()){
            case LongPollService.Update.TYPE_OFFLINE:
            case LongPollService.Update.TYPE_ONLINE:
                return Memory.isTracked(update.getUser());
            case LongPollService.Update.TYPE_CHAT_TYPING:
                return false;
            case LongPollService.Update.TYPE_USER_TYPING:
            default:
                return true;
        }
    }
    public static void showPopup(ArrayList<Event> events) {


        SharedPreferences preferences = context.getSharedPreferences("popup", Activity.MODE_MULTI_PROCESS);
        if(!preferences.getBoolean("status",true) || events.isEmpty()){
            return;
        }
        Toast toast = new Toast(context);


        LinearLayout rootView = new LinearLayout(context);
        rootView.setOrientation(LinearLayout.VERTICAL);

        rootView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_message));

        for (Event event : events) {
            rootView.addView(event.getView(context));
        }

        toast.setView(rootView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
    private static void showNotification(ArrayList<Event> notifyEvents) {

        for(Event event: notifyEvents) {

            final NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_stat_spy)
                            .setContentTitle(event.headerText)
                            .setContentText(event.messageText)
                            .setAutoCancel(true)
                            .setDefaults( Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                            .setContentInfo("VK Spy");

            SharedPreferences prefs = context.getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);

            String ringtoneUri = prefs.getString("notifications_ringtone", "");
            Uri parsedRingtonUri = Uri.parse(ringtoneUri);
            mBuilder.setSound(parsedRingtonUri);


            Intent resultIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            final int mId = event.id;

            ImageLoader.getInstance().loadImage(event.imageUrl,new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                    mBuilder.setLargeIcon(bitmap);
                    Notification notification = mBuilder.build();
                    mNotificationManager.notify(mId, notification);

                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });

        }
    }
    public static void DESTROY() {
        context = null;
    }

    public static class Event {

        private  int id;
        public String headerText;
        public String messageText;
        public String imageUrl;

        public Event(String headerText, String messageText, String imageUrl,int id) {
            this.headerText = headerText;
            this.messageText = messageText;
            this.imageUrl = imageUrl;
            this.id = id;
        }

        public Event(LongPollService.Update update) {

            this.headerText = update.getHeader();
            this.messageText = update.getMessage();
            this.imageUrl = update.getImageUrl();
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

    }
}