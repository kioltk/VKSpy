package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Online {
    public final int userId;
    private final int since;
    private final int till;

    public Online(int userId, int since, int till) {
        this.userId = userId;
        this.since = since;
        this.till = till;
    }
    public String getSinceShort(){
        if(since==0){
            return "undefined";
        }
        if(since==-1){
            return "online";
        }
       return Helper.getTimeShort(since);
    }
    public Integer getUnix() {
        if(till>0)
            return till;
        return since;
    }

    public String getTillShort() {

        if(since==0){
            return "undefined";
        }
        return Helper.getTimeShort(till);
    }
}
