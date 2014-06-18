package com.happysanta.spy.Models;

import com.happysanta.spy.Core.Memory;
import com.happysanta.spy.Helper.Time;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class Update implements Comparable {
    public final int id;
    protected final int unix;
    protected int userid;

    protected String convertedSmartTime;
    protected String convertedTime;
    protected String convertedDate;

    protected VKApiUserFull owner;

    public Update(int userid, int unix, int id){
        this(Memory.getUserById(userid),unix,id);
    }

    public Update(VKApiUserFull user, int unix, int id) {
        this.owner = user;
        this.unix = unix;
        this.id = id;
    }

    public VKApiUserFull getOwner(){
        return owner;
    }

    protected String convertTime() {
        return Time.getTime(getUnix());
    }
    protected String convertDate(){
        return Time.getDate(getUnix());
    }
    protected String convertSmartTime(){
        return Time.getSmartTime(getUnix());
    }

    public String getDate(){
        return convertedDate;
    }
    public String getTime(){
        return convertedTime;
    }
    public String getSmartTime(){
        return convertedSmartTime;
    }

    public Boolean compareDays(Update anotherUpdate) {
        return anotherUpdate != null && Time.checkOneDay(anotherUpdate.getUnix(), this.getUnix());
    }

    public Integer getUnix(){
        return unix;
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof Update)
            return ((Integer)this.id).compareTo(((Update)another).id);
        return 0;
    }
}
