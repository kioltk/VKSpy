package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

public class Online extends Update {
    private int since;
    private int till;

    public Online(int userid, int since, int till) {
        super(userid);

        this.since = since;
        this.till = till;
    }
    public String getSinceShort(){
        if(since==0){
            return "undefined";
        }
       return Helper.getTimeShort(since);
    }
    @Override
    public Integer getUnix() {
        if(till>0)
            return till;
        return since;
    }


    public String getTillShort() {

        if(till==0){
            return "undefined";
        }

        if(till==-1){
            return "online";
        }
        return Helper.getTimeShort(till);
    }


    @Override
    public String getTime() {
        return Helper.getTime(getUnix());
    }

    public String getDate(){
        return Helper.getDate(getUnix());
    }

    public int getTill() {
        return till;
    }

    public void setTill(int till) {
        this.till = till;
    }

    public boolean isOnline() {
        if(till==-1)
            return getOwner().online;
        return false;
    }

    public int getSince() {
        return since;
    }

    public boolean isStreak() {
        return till > 0 && since > 0;
    }

    public String getSinceTime() {
        return Helper.getTime(getSince());
    }

    public String getTillTime() {
        return Helper.getTime(getTill());
    }

    public String getStreak() {
        return Helper.getStreak(till, since);
    }
}
