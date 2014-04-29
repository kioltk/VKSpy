package com.agcy.vkproject.spy.Adapters;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.TypingItem;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.Models.Update;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class ItemHelper {
    public static ArrayList<Item> convertUpdates (ArrayList<? extends Update> updates) {
        ArrayList<Item> items = new ArrayList<Item>();
        Update lastUpdate = null;
        for(Update update:updates) {
            if (!update.compareDays(lastUpdate))
                items.add(new DateItem(update.getUnix()));
            Item item = null;
            if (update instanceof Online)
                item = new OnlineItem((Online) update);
            if (update instanceof Typing)
                item = new TypingItem((Typing) update);
            items.add(item);
            lastUpdate = update;
        }
        return items;
    }
}
