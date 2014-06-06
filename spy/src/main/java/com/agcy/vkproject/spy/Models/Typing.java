package com.agcy.vkproject.spy.Models;

import com.vk.sdk.api.model.VKApiUserFull;

public class Typing extends Update {
    public Typing(int userid,int time){
        super(userid,time);

        convertedSmartTime = convertSmartTime();
        // :O нарефакторился, что класс совсемушки пустой стал(
    }

    @Override
    public String getSmartTime() {
        return convertSmartTime();
    }

    public Typing(VKApiUserFull user, int unixNow) {
        super(user, unixNow);

        convertedSmartTime = convertSmartTime();
    }
}
