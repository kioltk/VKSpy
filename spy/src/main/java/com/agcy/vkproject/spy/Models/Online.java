package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;

public class Online extends Update {

    private int since;
    private int till;

    protected String sinceConvertedTime;
    protected String tillConvertedTime;

    private final String streakConverted;
    public Online(int userid, int since, int till) {
        super(userid, till > 0 ? till: since);

        this.since = since;
        this.till = till;

        sinceConvertedTime =  Helper.getTime(since);
        tillConvertedTime = Helper.getTime(till);
        streakConverted = Helper.getStreak(till, since);

    }

    @Override
    public String getTime(){
        return since > 0 ? getSinceTime() : getTillTime();
    }

    /**
    public String getSinceShort(){
        if(since==0){
            return "undefined";
        }
       return Helper.getTimeShort(since);
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
    */



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
        return sinceConvertedTime;
    }

    public String getTillTime() {
        return tillConvertedTime;
    }

    public String getStreak() {
        return streakConverted;
    }

}
