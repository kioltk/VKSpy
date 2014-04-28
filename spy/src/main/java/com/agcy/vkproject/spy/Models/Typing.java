package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Typing {
    private final int time;
    public final int userId;
    public Typing(int userId,int time){
        this.time = time;
        this.userId = userId;
    }
    public String getTime(){
        return Helper.getTime(time);
    }
}
