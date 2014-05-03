package com.agcy.vkproject.spy.Core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;

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
        ArrayList<Event> events = new ArrayList<Event>();
        for (LongPollService.Update update : updates) {
            if(isUpdateTracked(update))
                events.add(new Event(update.getHeader(), update.getMessage(), update.getImageUrl()));
        }
        showPopup(events);

    }
    public static Boolean isUpdateTracked(LongPollService.Update update){
        switch (update.getType()){
            case LongPollService.Update.TYPE_OFFLINE:
            case LongPollService.Update.TYPE_ONLINE:
                return Memory.isTracked(update.getUser());
            case LongPollService.Update.TYPE_CHAT_TYPING:
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
        //todo: define way to show popup
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

    public static void DESTROY() {
        context = null;
    }

    public static class Event {

        public String headerText;
        public String messageText;
        public String imageUrl;

        public Event(String headerText, String messageText, String imageUrl) {
            this.headerText = headerText;
            this.messageText = messageText;
            this.imageUrl = imageUrl;
        }

        public Event(LongPollService.Update update) {

            this.headerText = update.getHeader();
            this.messageText = update.getMessage();
            this.imageUrl = update.getImageUrl();
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