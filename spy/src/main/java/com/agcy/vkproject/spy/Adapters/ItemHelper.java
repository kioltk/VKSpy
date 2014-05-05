package com.agcy.vkproject.spy.Adapters;

import android.util.Log;

import com.agcy.vkproject.spy.Adapters.CustomItems.DateItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.HeaderItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.OnlineItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.StatusItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.TypingItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.UserItem;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.Models.Typing;
import com.agcy.vkproject.spy.Models.Update;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kiolt_000 on 28-Apr-14.
 */
public class ItemHelper {
    private static ArrayList<Item> convertUpdates (List<? extends Update> updates,boolean first) {
        ArrayList<Item> items = new ArrayList<Item>();
        Update lastUpdate = null;
        if(!first){
            lastUpdate = updates.get(0);
        }
        for(Update update:updates) {
            if (!update.compareDays(lastUpdate))
                items.add(new DateItem(update.getUnix()));
            Item item = null;
            if (update instanceof Online)
                item = new OnlineItem((Online) update);
            if(update instanceof Status)
                item = new StatusItem((Status) update);
            if (update instanceof Typing)
                item = new TypingItem((Typing) update);
            items.add(item);
            lastUpdate = update;
        }
        return items;
    }
    private static ArrayList<Item> convertUsers (List<? extends VKApiUserFull> users,boolean first) {
        ArrayList<Item> convertedUsers = new ArrayList<Item>();
        if(first) {
            convertedUsers.add(new HeaderItem("Important"));

            for (VKApiUserFull user : users.subList(0, 5)) {
                convertedUsers.add(new UserItem(user));
            }
        }
        ArrayList<VKApiUserFull> tempUsers = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users) {
            tempUsers.add(user);
        }

        Collections.sort(tempUsers, new Comparator<VKApiUserFull>() {
            @Override
            public int compare(VKApiUserFull lhs, VKApiUserFull rhs) {
                return lhs.first_name.compareTo(rhs.first_name);
            }
        });


        VKApiUserFull lastUser = null;
        for(VKApiUserFull user:tempUsers) {
            if (!theSameLastNameLetter(user,lastUser))
                convertedUsers.add(new HeaderItem(user.first_name.substring(0,1)));
            Item item = new UserItem(user);
            convertedUsers.add(item);
            lastUser = user;
        }
        return convertedUsers;
    }

    private static boolean theSameLastNameLetter(VKApiUserFull user1, VKApiUserFull user2){
        if(user1 == null || user2 == null){
            return false;
        }
        return user1.first_name.substring(0,1).equals(user2.first_name.substring(0,1));
    }
    public static class ObservableUpdatesArray extends ObservableArray<Update>{
        public ObservableUpdatesArray(ArrayList<? extends Update> items) {
            super(items);
        }

        @Override
        public List<Item> convert(List<? extends Update> source,boolean first) {
            return convertUpdates(source,first);
        }
    }
    public static class ObservableUsersArray extends ObservableArray<VKApiUserFull>{
        public ObservableUsersArray(ArrayList<? extends VKApiUserFull> items) {
            super(items,items.size());
        }

        @Override
        public List<Item> convert(List<? extends VKApiUserFull> source,boolean first) {
            return convertUsers(source,first);
        }
    }

    public static abstract class ObservableArray<T>{

        public abstract List<Item> convert(List<? extends T> source,boolean first);

        protected final ArrayList<? extends T> items;
        protected final ArrayList<Item> convertedItems = new ArrayList<Item>();
        private int offset = 0;
        public ObservableArray(ArrayList<? extends T> items, int startCount) {
            this.items = items;
            if (items.size() != 0) {

                convertMore(startCount,true);
            }
        }
        public ObservableArray(ArrayList<? extends T> items) {
            this(items,25);
        }
        public Boolean convertMore(int count){
            return convertMore(count, false);
        }
        protected Boolean convertMore(int count,boolean first){
            //Log.i("AGCY SPY", "Converting. Items: " + adapter.size()+", offset: "+offset+", count: "+count);
            try {
                int itemsCount = items.size();
                if(offset == itemsCount)
                    return false;

                int least = items.size() - offset;
                if (count > least){
                    count = least;
                }
                convertedItems.addAll(convert(items.subList(offset, offset + count),first));
                offset = offset+ count;
            }
            catch(Exception exp){
                Log.wtf("AGCY SPY", "WOWOW chill out guy");
                return false;
            }
            return true;
        }
        public boolean isLast(int position){
            return offset == items.size() && position+1 == convertedItems.size();

        }
        public int size(){
            return convertedItems.size();
        }
        public Item get(int position){
            return convertedItems.get(position);
        }
    }
}
