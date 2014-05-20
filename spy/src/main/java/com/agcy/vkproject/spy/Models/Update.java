package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class Update {
    protected final int unix;
    protected int userid;

    protected String convertedSmartTime;
    protected String convertedTime;
    protected String convertedDate;

    protected VKApiUserFull owner;

    public Update(int userid, int unix){

        this.userid = userid;
        this.unix = unix;

        //convertedDate = convertDate();

        owner = Memory.getUserById(userid);

    }

    public VKApiUserFull getOwner(){
        return owner;
    }

    protected String convertTime() {
        return Helper.getTime(getUnix());
    }
    protected String convertDate(){
        return Helper.getDate(getUnix());
    }
    protected String convertSmartTime(){
        return Helper.getSmartTime(getUnix());
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
        return anotherUpdate != null && Helper.checkOneDay(anotherUpdate.getUnix(), this.getUnix());
    }

    public Integer getUnix(){
        return unix;
    }


}
