package com.happysanta.crazytyping.Models;

import com.happysanta.crazytyping.Core.Memory;
import com.vk.sdk.api.model.VKApiUserFull;

public class Typing extends Update {
    public Typing(int userid,int time){
        this(Memory.getUserById(userid),time, 0);

        convertedSmartTime = convertSmartTime();
        // :O нарефакторился, что класс совсемушки пустой стал(
    }

    @Override
    public String getSmartTime() {
        return convertSmartTime();
    }

    public Typing(VKApiUserFull user, int unixNow, int id) {
        super(user, unixNow,id);

        convertedSmartTime = convertSmartTime();
    }
}
