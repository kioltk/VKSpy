package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public abstract class Update {
    protected int userid;

    public Update(int userid){
        this.userid = userid;
    }

    public VKApiUserFull getOwner(){
        return Memory.getUserById(userid);
    }
    public abstract String getTime();

    public Boolean compareDays(Update anotherUpdate) {
        return anotherUpdate != null && getDate().equals(anotherUpdate.getDate());
    }
    public abstract String getDate();

    public abstract Integer getUnix();

    public String getSmartTime(){
        return Helper.getSmartTime(getUnix());
    }
}
