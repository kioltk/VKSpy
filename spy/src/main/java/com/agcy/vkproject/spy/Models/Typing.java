package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Typing extends Update {
    private final int time;
    public Typing(int userid,int time){
        super(userid);
        this.time = time;
    }
    @Override
    public String getTime(){
        return Helper.getTime(time);
    }

    @Override
    public String getDate(){
        return Helper.getDate(time);
    }
    @Override
    public Integer getUnix() {
        return time;
    }
}
