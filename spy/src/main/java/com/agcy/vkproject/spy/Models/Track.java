package com.agcy.vkproject.spy.Models;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class Track {
    public final int userid;
    public Boolean notification;
    public Track(int userid, Boolean notification){
        this.userid = userid;
        if(notification==null)
            this.notification = false;
        else
            this.notification = notification;
    }

    public Track(int userid) {
        this(userid,null);
    }

    public Track toggle() {
        notification = !notification;
        return this;
    }
}
