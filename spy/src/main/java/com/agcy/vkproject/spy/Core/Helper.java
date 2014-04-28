package com.agcy.vkproject.spy.Core;

import com.agcy.vkproject.spy.Models.Online;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class Helper {
    public static ArrayList<Online> orderOnlines(ArrayList<Online> onlines,Boolean asc) {
        Collections.sort(onlines, new CustomComparator());
        if(!asc)
            Collections.reverse(onlines);
        return onlines;
    }
    public static class CustomComparator implements Comparator<Online> {
        @Override
        public int compare(Online o1, Online o2) {
            return o1.getUnix().compareTo(o2.getUnix());
        }
    }

    public static String getTime(int unix){

        Date date = new Date(unix * 1000L);

        SimpleDateFormat ft =
                new SimpleDateFormat("KK:mm:ss");
        return ft.format(date);
    }
    public static String getTimeShort(int unix){

        Date date = new Date(unix * 1000L);

        SimpleDateFormat ft =
                new SimpleDateFormat("KK:mm");
        return ft.format(date);
    }
}
