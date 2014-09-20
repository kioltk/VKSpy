package com.happysanta.vkspy.Helper;

import android.os.Handler;
import android.util.Log;

import com.happysanta.vkspy.Core.Memory;
import com.happysanta.vkspy.Core.Notificator;
import com.happysanta.vkspy.Longpoll.LongPollService;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kioltk on 6/16/14.
 */
public class Type {
    public static ArrayList<Runnable> timerListeners = new ArrayList<Runnable>();
    static HashMap<Integer, TypingTimer> typingTimers = new HashMap<Integer, TypingTimer>();
    static HashMap<Integer, ChatTimer> chatTimers = new HashMap<Integer, ChatTimer>();
    private static Handler typingHandler = new Handler();

    public static void newChatTyping(final LongPollService.Update update) {

        Integer chatid = (Integer) update.getExtra();
        Memory.getChatById(chatid);
        update.getUser();
        if (chatTimers.containsKey(chatid)) {
            chatTimers.get(chatid).again(update);
        } else {
            ChatTimer chatTimer = new ChatTimer();
            chatTimer.start(update);

            chatTimers.put(chatid, chatTimer);
        }
    }

    public static void newTyping(final LongPollService.Update typing) {

        typing.getUser();
        if (typingTimers.containsKey(typing.getUserId())) {
            typingTimers.get(typing.getUserId()).again();
        } else {

            TypingTimer typingTimer = new TypingTimer(typing);
            typingTimer.start();
            typingTimers.put(typing.getUserId(), typingTimer);
        }

    }

    public static void denyChatTyping(int userid, int chatid) {

        if (chatTimers.containsKey(chatid)) {
            chatTimers.get(chatid).deny(userid);
        }
    }

    public static void denyTyping(final LongPollService.Update update) {
        if (update.getExtra().equals(0)) {
            if (typingTimers.containsKey(update.getUserId())) {
                typingTimers.get(update.getUserId()).deny(new Runnable() {
                    @Override
                    public void run() {
                        typingTimers.remove(update.getUserId());
                    }
                });
            }
        } else {
            denyChatTyping(update.getUserId(), (Integer) update.getExtra());
        }
    }

    public static void showTyping(final LongPollService.Update update) {
        VKApiUserFull user = update.getUser();
        if (update.getExtra().equals(0)) {
            Memory.setTyping(user);
        } else {
            VKApiChat chat = Memory.getChatById((Integer) update.getExtra());
            Memory.setChatTyping(user, chat);
        }

        Notificator.announce(new ArrayList<LongPollService.Update>() {{
            add(update);
        }});
    }

    public static void newChatMessage(LongPollService.Update update) {
        denyChatTyping(update.getUserId(), (Integer) update.getExtra());
    }

    public static void newMessage(LongPollService.Update message) {
        denyTyping(message);
    }

    public static void stopTimers() {
        for (TypingTimer typingTimer : typingTimers.values()) {
            typingTimer.deny();
        }
        typingTimers.clear();

        for (ChatTimer chatTimer : chatTimers.values()) {
            for (TypingTimer typingTimer : chatTimer.values()) {
                typingTimer.deny();
            }
        }
        chatTimers.clear();
    }

    static class ChatTimer extends HashMap<Integer, TypingTimer> {

        public void start(LongPollService.Update update) {

            TypingTimer typingTimer = new TypingTimer(update);
            put(update.getUserId(), typingTimer);
            typingTimer.start();
        }

        public void again(LongPollService.Update update) {
            if (containsKey(update.getUserId())) {
                get(update.getUserId()).again();
            } else {
                start(update);
            }
        }

        public void deny(final int userid) {

            if (containsKey(userid)) {

                TypingTimer timer = get(userid);
                timer.deny(new Runnable() {
                    @Override
                    public void run() {

                        remove(userid);
                    }
                });
            }
        }
    }

    static class TypingTimer extends Thread {
        public LongPollService.Update update;
        private boolean again = false;
        private boolean denied = false;
        private Runnable denyCallback;
        private boolean finished = false;

        public TypingTimer(LongPollService.Update update) {
            this.update = update;
        }

        @Override
        public void run() {
            Log.i("AGCY SPY THREAD", "TypingTimer runs userid: " + update.getUserId() + ", extras: " + update.getExtra());
            do {
                again = false;
                try {
                    // Ждем три минуты
                    Thread.sleep(3 * 60 * 1000);
                    // если дождались, тогда постим, что споймали новый тайпинг
                    finished = true;
                    typingHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showTyping(update);
                            denyTyping(update);
                        }
                    });
                    Log.i("AGCY SPY THREAD", "TypingTimer ended userid: " + update.getUserId() + ", extras: " + update.getExtra());
                    return;
                } catch (InterruptedException e) {
                    // если оборвалось
                    e.printStackTrace();
                    if (denied) {
                        try {
                            Thread.sleep(20 * 1000);
                            if (denyCallback != null)
                                denyCallback.run();
                        } catch (InterruptedException e1) {
                            denied = false;
                        }
                        Log.i("AGCY SPY THREAD", "TypingTimer denied userid: " + update.getUserId() + ", extras: " + update.getExtra());

                        return;
                    }
                    if (again) {
                        // обрыв, чтобы начать заново? тогда повторяем круг
                        Log.i("AGCY SPY THREAD", "TypingTimer repeated userid: " + update.getUserId() + ", extras: " + update.getExtra());
                        continue;
                    }
                    return;
                }

            } while (again);
        }

        public void again() {
            if (denied)
                return;

            this.again = true;
            interrupt();
            Log.i("AGCY SPY THREAD", "TypingTimer rerun userid: " + update.getUserId() + ", extras: " + update.getExtra());
        }

        public void deny() {
            deny(null);
        }

        public void deny(Runnable runnable) {
            if (finished) {
                runnable.run();
                return;
            }
            if (denied)
                return;
            denied = true;
            denyCallback = runnable;
            interrupt();


        }
    }
}