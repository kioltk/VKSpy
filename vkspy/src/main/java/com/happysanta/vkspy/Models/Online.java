package com.happysanta.vkspy.Models;

import com.happysanta.vkspy.Helper.Time;

public class Online extends Update {

    private int since;
    private int till;
    private int platform = 7;
    protected String sinceConvertedTime;
    protected String tillConvertedTime;

    private final String streakConverted;
    public Online(int userid, int since, int till, int id) {
        super(userid, till > 0 ? till: since, id);

        this.since = since;
        this.till = till;

        sinceConvertedTime =  Time.getTime(since);
        tillConvertedTime = Time.getTime(till);
        streakConverted = Time.getStreak(till, since);

    }
    public Online(int userid, int since, int till, int id, Integer platform){
        this(userid,  since,  till,  id);
        if(platform!=null)
        this.platform = platform;
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

    public int getPlatform() {
        return platform;
    }
}
