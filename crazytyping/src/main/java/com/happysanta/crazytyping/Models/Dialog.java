package com.happysanta.crazytyping.Models;

import com.happysanta.crazytyping.Core.Memory;
import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kioltk on 6/19/14.
 */
public class Dialog {
    protected final int userid;
    private VKApiUserFull user;
    public boolean targeted;

    public Dialog(int userid, Boolean targeted) {
        this.userid = userid;
        this.targeted = targeted;
        if(!(this instanceof ChatDialog)){
            this.user = Memory.getUserById(userid);
        }
    }
    public int getUserid(){
        return userid;
    }

    public String getTitle() {
        if(user!=null)
            return user.toString();
        return "HappySanta";
    }
    public String getPhoto(){
        return user.getBiggestPhoto();
    }

    public long getId() {
        return getUserid();
    }
}
