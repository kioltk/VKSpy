package com.happysanta.spy.Models;

import com.happysanta.spy.Core.Memory;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiUserFull;


public class ChatTyping extends Typing {
    private final VKApiChat chat;

    public ChatTyping(VKApiUserFull user, int unixNow, VKApiChat chat) {
        super(user, unixNow, 0);
        this.chat = chat;
    }

    public ChatTyping(int userid, int time, int chatid) {
        super(userid, time);
        this.chat = Memory.getChatById(chatid);
    }

    public VKApiChat getChat() {
        return chat;
    }
}
