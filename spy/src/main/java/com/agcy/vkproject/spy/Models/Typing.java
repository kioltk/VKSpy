package com.agcy.vkproject.spy.Models;

import com.vk.sdk.api.model.VKApiUserFull;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Typing extends Update {
    public Typing(int userid,int time){
        super(userid,time);

        convertedSmartTime = convertSmartTime();
        // :O нарефакторился, что класс совсемушки пустой стал(
    }

    public Typing(VKApiUserFull user, int unixNow) {
        super(user, unixNow);

        convertedSmartTime = convertSmartTime();
    }
}
