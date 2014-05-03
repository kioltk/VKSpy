package com.agcy.vkproject.spy.Adapters;

import android.util.Log;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.TypingItem;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.Models.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class ItemHelper {
    private static ArrayList<Item> convertUpdates (List<? extends Update> updates) {
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
    public static class ObservableArray {
        private final ArrayList<? extends Update> items;
        private final ArrayList<Item> convertedItems = new ArrayList<Item>();
        private int offset = 0;
        public ObservableArray(ArrayList<? extends Update> items) {
            this.items = items;
            if (items.size() != 0) {

                convertMore(25);
            }
        }
        public Boolean convertMore(int count){
            //Log.i("AGCY SPY", "Converting. Items: " + items.size()+", offset: "+offset+", count: "+count);
            try {
                int itemsCount = items.size();
                if(offset == itemsCount)
                    return false;

                int least = items.size() - offset;
                if (count > least){
                    count = least;
                }
                convertedItems.addAll(convertUpdates(items.subList(offset, offset + count)));
                offset = offset+ count;
            }
            catch(Exception exp){
                Log.wtf("AGCY SPY", "WOWOW chill out guy");
                return false;
            }
            return true;
        }
        public int size(){
            return convertedItems.size();
        }
        public Item get(int position){
            return convertedItems.get(position);
        }
    }
}
