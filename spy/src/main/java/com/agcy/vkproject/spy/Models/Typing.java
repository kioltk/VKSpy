package com.agcy.vkproject.spy.Models;

import com.agcy.vkproject.spy.Core.Memory;
import com.vk.sdk.api.model.VKApiUserFull;

public class Typing extends Update {
    public Typing(int userid,int time){
        this(Memory.getUserById(userid),time);

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
