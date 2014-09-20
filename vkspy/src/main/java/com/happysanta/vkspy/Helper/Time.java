package com.happysanta.vkspy.Helper;

import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;

import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Core.Memory;
import com.happysanta.vkspy.R;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.Date;

/**
 * Created by kioltk on 6/16/14.
 */
public class Time {

    private static int timeFormat = 0;
    public static int getUnixNow() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    public static boolean isOnline(int userid) {
        return Memory.getUserById(userid).online;
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
            case Helper.NOW:
                return Helper.getContext().getString(R.string.now);
            case Helper.UNDEFINED:
                return Helper.getContext().getString(R.string.undefined);
        }

        Date date = new Date(unix * 1000L);

        if (getTimeFormat() == 24) {

            return String.format("%tT",
                    date);

        }
        int hrs = date.getHours();
        Boolean isPM = hrs > 12;
        return String.format("%1$tl:%1$tM:%1$tS " + (isPM ? "pm" : "am"),
                date);

    }

    public static String getSmartDate(int time) {
        Resources res = Helper.getContext().getResources();
        if (time == Helper.NOW)
            return res.getString(R.string.now);
        if(time== Helper.UNDEFINED){
            return res.getString(R.string.undefined);
        }
        if (isToday(time))
            return res.getString(R.string.today);
        if (getDate( getUnixNow() - 24 * 3600).equals(getDate(time)))
            return res.getString(R.string.yesterday);
        return getDate(time);
    }

    public static String getSmartTime(Integer unix) {
        Resources res = Helper.getContext().getResources();
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

    public static String getStreak(int till, int since) {

        Resources res = Helper.getContext().getResources();
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
        Resources res = Helper.getContext().getResources();
        String lastSeen = res.getString(isFemale ? R.string.last_seen_f : R.string.last_seen) + " ";
        if(getUnixNow()-time<60)
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

    public static String getSmartDateTime(long time) {
        return getSmartTime((int) time);
    }

    public static int getTimeFormat() {
        if(timeFormat != 0)
            return timeFormat;
        try {
            int time_format = Settings.System.getInt(Helper.getContext().getContentResolver(), Settings.System.TIME_12_24);
            timeFormat = time_format;
            Log.i("AGCY SPY", "Time format detected: " + time_format);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            timeFormat = 24;
        }
        return timeFormat;
    }
}
